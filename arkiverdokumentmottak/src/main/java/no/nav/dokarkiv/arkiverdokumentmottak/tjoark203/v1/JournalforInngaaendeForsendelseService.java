package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;


import static no.nav.dokarkiv.arkiverdokumentmottak.ServiceConstants.FORSENDELSE_MOTTAK_ID_KEY;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.nsb.DokumentInfoIdVedleggTo;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;

@Service
@Slf4j
public class JournalforInngaaendeForsendelseService {

	@Inject
	private JoarkRepository joarkRepository;
	@Inject
	private DokumentFilerDelegate dokumentFilerDelegate;
	@Inject
	private JournalforInngaaendeForsendelseValidator validator;

	public JournalforInngaaendeForsendelseResponseTo journalforInngaaendeForsendelse(
			JournalforInngaaendeForsendelseRequestTo requestTo) {
		requestTo.validate();
		String tillegsopplysning = requestTo.getJournalpost().getTilleggsopplysninger().get(FORSENDELSE_MOTTAK_ID_KEY);
		log.info("TJOARK203_V1 Sjekker om journalpost med tillegsopplysning.ForsendelseMottakId={} finnes fra før", tillegsopplysning);
		Journalpost storedJournalpost = findPreviousJournalforing(requestTo);
		if (storedJournalpost == null) {
			log.info("TJOARK203_V1 Fant ingen journalpost med tillegsopplysning.ForsendelseMottakId={}, oppretter ny journalpost", tillegsopplysning);
			Journalpost journalpost = requestTo.getJournalpost();
			updateJournalpost(journalpost);
			validator.validate(journalpost, true);

			dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
			storedJournalpost = joarkRepository.save(journalpost);
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
		if (forsendelseMottakId == null || forsendelseMottakId.isEmpty()) {
			return null;
		}

		Long journalpostId = joarkRepository.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(FORSENDELSE_MOTTAK_ID_KEY, forsendelseMottakId);
		if(journalpostId == null) {
			return null;
		}
		return joarkRepository.findById(journalpostId).orElse(null);
	}

}
