package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.arkiverdokumentmottak.DokumentInfoIdVedleggTo;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
	private static final String JOURNALFORENDE_ENHET_9999 = "9999";

	@Inject
    private JoarkRepositorySkjermet joarkRepository;
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
			storedJournalpost = joarkRepository.save(journalpost);
			log.info("TJOARK203_V2 Har journalført journalpost med journalpostId={}, hoveddokumentDokumentInfoId={}, journalstatus={}, kanalreferanseId={}, mottaksKanal={}, fagområde={}", storedJournalpost
							.getJournalpostId(), storedJournalpost.findHoveddokumentDokumentInfoRelasjon()
							.getDokumentInfo()
							.getDokumentInfoId(),
					storedJournalpost.getJournalstatus(), storedJournalpost.getKanalReferanseId(), storedJournalpost.getMottakskanal(), storedJournalpost
							.getFagomrade());
			return buildResponse(storedJournalpost);
		}
		log.info("TJOARK203_V2 Journalpost med journalpostId={}, kanalReferanseId={} og mottaksKanal={} eksisterer allerede i databasen.", storedJournalpost
						.getJournalpostId(), storedJournalpost.getKanalReferanseId(),
				storedJournalpost.getMottakskanal());
		return buildResponse(storedJournalpost);
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
				DokumentInfoIdVedleggTo vedlegg = DokumentInfoIdVedleggTo.builder()
						.dokumentInfoId(rel.getDokumentInfo().getDokumentInfoId())
						.dokumentTypeId(rel.getDokumentInfo().getDokumenttypeId())
						.build();
				to.getDokumentInfoIdVedleggTo().add(vedlegg);
			}
		}
		return to;
	}

	private void updateJournalpostAfterValidation(Journalpost journalpost) {
		if (journalpost.getJournalstatus() == JournalStatusCode.J) {
			journalpost.setJournalDato(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
			journalpost.setJournalfortAvNavn(journalpost.getOpprettetAvNavn());

			if (journalpost.getJournalForendeEnhetId() == null || journalpost.getJournalForendeEnhetId().isEmpty()) {
				journalpost.setJournalForendeEnhetId(JOURNALFORENDE_ENHET_9999);
			}
		}
	}

	private Journalpost findPreviousJournalforing(Journalpost journalpost) {
		if (Strings.isNullOrEmpty(journalpost.getKanalReferanseId()) || Strings.isNullOrEmpty(journalpost.getMottakskanal()
				.name())) {
			return null;
		}

        return joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(journalpost.getKanalReferanseId(),
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

	private void updateJournalpostBeforeValidation(Journalpost journalpost) {
		//This is just a dummy assignment - the final value of Journalstatus is set by decideAndSetJournalStatus()
		journalpost.setJournalstatus(JournalStatusCode.J);

		journalpost.setJournalposttype(JournalpostTypeCode.I);

		for (JournalpostDokumentInfoRelasjon rel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			rel.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn());
		}
	}
}
