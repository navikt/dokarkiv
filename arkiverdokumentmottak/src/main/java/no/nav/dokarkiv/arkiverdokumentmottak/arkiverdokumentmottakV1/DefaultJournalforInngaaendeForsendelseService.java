package no.nav.dokarkiv.arkiverdokumentmottak.arkiverdokumentmottakV1;

import static no.nav.service.dok.joark.ServiceConstants.FORSENDELSE_MOTTAK_ID_KEY;

import com.google.common.base.Strings;
import no.nav.domain.dok.joark.Journalpost;
import no.nav.domain.dok.joark.JournalpostDokumentInfoRelasjon;
import no.nav.domain.dok.joark.codestable.JournalStatusCode;
import no.nav.domain.dok.joark.codestable.JournalpostTypeCode;
import no.nav.domain.dok.joark.codestable.TilknyttetJournalpostSomCode;
import no.nav.repository.dok.joark.mod.JoarkRepository;
import no.nav.repository.dok.joark.util.DateProvider;
import no.nav.service.dok.joark.journalbehandling.DokumentFilerDelegate;
import no.nav.service.dok.joark.nsb.JournalforInngaaendeForsendelseValidator;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;

public class DefaultJournalforInngaaendeForsendelseService implements JournalforInngaaendeForsendelseService {

	@Inject
	private JoarkRepository joarkRepository;
	@Inject
	private DokumentFilerDelegate dokumentFilerDelegate;
	@Inject
	private JournalforInngaaendeForsendelseValidator validator;

	@Override
	public JournalforInngaaendeForsendelseResponseTo journalforInngaaendeForsendelse(
			JournalforInngaaendeForsendelseRequestTo requestTo) {
		requestTo.validate();
		Journalpost storedJournalpost = findPreviousJournalforing(requestTo);
		if (storedJournalpost == null) {
			Journalpost journalpost = requestTo.getJournalpost();
			updateJournalpost(journalpost);
			validator.validate(journalpost, true);

			dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
			storedJournalpost = joarkRepository.saveNewJournalPost(journalpost);
			return buildResponse(storedJournalpost);
		}
		return buildResponse(storedJournalpost);
	}

	private void updateJournalpost(Journalpost journalpost) {
		journalpost.setJournalstatus(JournalStatusCode.J);
		journalpost.setJournalposttype(JournalpostTypeCode.I);
		journalpost.setJournalDato(DateProvider.getToday());

		for (JournalpostDokumentInfoRelasjon rel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			rel.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn());
		}
	}

	private JournalforInngaaendeForsendelseResponseTo buildResponse(Journalpost journalpost) {
		JournalforInngaaendeForsendelseResponseTo to = new JournalforInngaaendeForsendelseResponseTo(journalpost.getJournalpostId());
		to.setDokumentInfoIdHoveddokument(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId());
		for (JournalpostDokumentInfoRelasjon rel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			if (rel.getTilknyttetJournalpostSom() == TilknyttetJournalpostSomCode.VEDLEGG) {
				DokumentInfoIdVedleggTo vedlegg = new DokumentInfoIdVedleggTo(rel.getDokumentInfo()
						.getDokumentInfoId(), rel.getDokumentInfo().getDokumenttypeId());
				to.getDokumentInfoIdVedleggTo().add(vedlegg);
			}
		}
		return to;
	}

	private Journalpost findPreviousJournalforing(JournalforInngaaendeForsendelseRequestTo requestTo) {
		if (CollectionUtils.isEmpty(requestTo.getJournalpost().getTilleggsopplysninger())) {
			return null;
		}

		String forsendelseMottakId = requestTo.getJournalpost().getTilleggsopplysninger().get(FORSENDELSE_MOTTAK_ID_KEY);
		if (Strings.isNullOrEmpty(forsendelseMottakId)) {
			return null;
		}

		Long findJournalpostTilleggssopplysning = joarkRepository.findJournalpostWithTilleggssopplysning(FORSENDELSE_MOTTAK_ID_KEY, forsendelseMottakId);
		return joarkRepository.findJournalpostById(findJournalpostTilleggssopplysning);
	}

}
