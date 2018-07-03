package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.nsb.DokumentInfoIdVedleggTo;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Service class for JournalforInngaaendeForsendelseV2 (TJOARK203)
 *
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@Component
public class JournalforInngaaendeForsendelseV2Service {

	private static final String JOURNALTILSTAND_ENDELIG = "ENDELIG";
	private static final String JOURNALTILSTAND_MIDLERTIDIG = "MIDLERTIDIG";

	@Inject
	private JoarkRepository joarkRepository;
	@Inject
	private DokumentFilerDelegate dokumentFilerDelegate;
	@Inject
	private JournalforInngaaendeForsendelseV2Validator validator;

	public JournalforInngaaendeForsendelseV2ResponseTo journalforInngaaendeForsendelseV2(
			JournalforInngaaendeForsendelseV2RequestTo requestTo) {
		requestTo.validate();
		Journalpost storedJournalpost = findPreviousJournalforing(requestTo.getJournalpost());
		if (storedJournalpost == null) {
			Journalpost journalpost = requestTo.getJournalpost();

			updateJournalpostBeforeValidation(journalpost);
			decideAndSetJournalStatus(requestTo.isForsokEndeligJf(), journalpost);
			validator.validateVariantFormaterAndHoveddokument(journalpost);
			updateJournalpostAfterValidation(journalpost);

			dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
			updateJournalpostDokumentInfoListOriginalJournalpost(journalpost);
			storedJournalpost = joarkRepository.save(journalpost);

			log.info("Opprettet journalpost med journalpostId=" + storedJournalpost.getJournalpostId() +
					". Journalstatus=" + storedJournalpost.getJournalstatus() + ". BrukerID(er)=" + retrieveAllBrukerIds(storedJournalpost)
					.toString());
			return buildResponse(storedJournalpost);
		}
		log.info("Journalpost med journalpostId=" + storedJournalpost.getJournalpostId() + " eksisterer allerede i databasen. BrukerID(er)=" +
				retrieveAllBrukerIds(storedJournalpost).toString());
		return buildResponse(storedJournalpost);
	}

	private void updateJournalpostDokumentInfoListOriginalJournalpost(Journalpost originalJournalpost) {

		for (JournalpostDokumentInfoRelasjon relasjon : originalJournalpost.getJournalpostDokumentInfoRelasjoner()) {
			relasjon.getDokumentInfo().setOriginalJournalpost(originalJournalpost);
		}
	}

	private JournalforInngaaendeForsendelseV2ResponseTo buildResponse(Journalpost journalpost) {
		JournalforInngaaendeForsendelseV2ResponseTo to = new JournalforInngaaendeForsendelseV2ResponseTo(journalpost.getJournalpostId());
		to.setDokumentInfoIdHoveddokument(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId());
		if (journalpost.getJournalstatus() == JournalStatusCode.J) {
			to.setJournalTilstand(JOURNALTILSTAND_ENDELIG);
		} else {
			to.setJournalTilstand(JOURNALTILSTAND_MIDLERTIDIG);
		}
		for (JournalpostDokumentInfoRelasjon rel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			if (rel.getTilknyttetJournalpostSom() == TilknyttetJournalpostSomCode.VEDLEGG) {
				DokumentInfoIdVedleggTo vedlegg = new DokumentInfoIdVedleggTo(rel.getDokumentInfo()
						.getDokumentInfoId(), rel.getDokumentInfo().getDokumenttypeId());
				to.getDokumentInfoIdVedleggTo().add(vedlegg);
			}
		}
		return to;
	}

	private void updateJournalpostAfterValidation(Journalpost journalpost) {
		if (journalpost.getJournalstatus() == JournalStatusCode.J) {
			journalpost.setJournalDato(DateProvider.getToday());
			journalpost.setJournalfortAvNavn(journalpost.getOpprettetAvNavn());
		}
	}

	private Journalpost findPreviousJournalforing(Journalpost journalpost) {
		if (Strings.isNullOrEmpty(journalpost.getKanalReferanseId()) || Strings.isNullOrEmpty(journalpost.getMottakskanal()
				.name())) {
			return null;
		}

		return joarkRepository.findJournalpostIdByKanalReferanseIdAndMottakskanal(journalpost.getKanalReferanseId(),
				journalpost.getMottakskanal().name()).orElse(null);
	}

	private void decideAndSetJournalStatus(Boolean isForsokEndeligJf, Journalpost journalpost) {
		if (isForsokEndeligJf) {
			try {
				validator.validate(journalpost);
				journalpost.setJournalstatus(JournalStatusCode.J);
			} catch (Exception e) {
				log.info("Required input parameter not set: " + e.getMessage() +
						". Setting JournalStatusCode = JournalStatusCode.M. JournalpostId = " + journalpost.getJournalpostId());
				journalpost.setJournalstatus(JournalStatusCode.M);
			}
		} else {
			journalpost.setJournalstatus(JournalStatusCode.M);
		}
	}

	private List<String> retrieveAllBrukerIds(Journalpost journalpost) {
		List<String> brukerIdList = new ArrayList<>();
		Iterator<Bruker> itr = journalpost.getBrukere().iterator();
		while (itr.hasNext()) {
			brukerIdList.add(itr.next().getBrukerId());
		}
		return brukerIdList;
	}

	private void updateJournalpostBeforeValidation(Journalpost journalpost) {
		//This is just a dummy assignment - the final value of Journalstatus is set by decideAndSetJournalStatus()
		journalpost.setJournalstatus(JournalStatusCode.J);

		journalpost.setJournalposttype(JournalpostTypeCode.I);

		for (JournalpostDokumentInfoRelasjon rel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			rel.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn());
		}
	}
}
