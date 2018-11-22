package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;


import static no.nav.dokarkiv.arkiverdokumentmottak.ArkiverDokumentmottakConstants.FORSENDELSE_MOTTAK_ID_KEY;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.arkiverdokumentmottak.DokumentInfoIdVedleggTo;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JoarkRepositoryBegrenset;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Slf4j
public class JournalforInngaaendeForsendelseService {

	@Inject
	private JoarkRepositoryBegrenset joarkRepository;
	@Inject
	private DokumentFilerDelegate dokumentFilerDelegate;
	@Inject
	private JournalforInngaaendeForsendelseValidator validator;

	public JournalforInngaaendeForsendelseResponseTo journalforInngaaendeForsendelse(
			JournalforInngaaendeForsendelseRequestTo requestTo) {
		requestTo.validate();

		String tillegsopplysning = getTillegsopplysning(requestTo);

		Journalpost storedJournalpost = findPreviousJournalforing(requestTo);
		if (storedJournalpost == null) {
			Journalpost journalpost = requestTo.getJournalpost();
			updateJournalpost(journalpost);
			validator.validate(journalpost);

			dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
			storedJournalpost = joarkRepository.save(journalpost);
			log.info("TJOARK203_V1 Har journalført journalpost med journalpostId={}, hoveddokumentDokumentInfoId={}, forsendelseMottakId={}, Journalstatus={}, Fagområde={}, MottaksKanal={}", storedJournalpost
							.getJournalpostId(), storedJournalpost.findHoveddokumentDokumentInfoRelasjon()
							.getDokumentInfo()
							.getDokumentInfoId(), tillegsopplysning,
					storedJournalpost.getJournalstatus(), storedJournalpost.getFagomrade(), storedJournalpost.getMottakskanal());
			return buildResponse(storedJournalpost);
		}

		log.info("TJOARK203_V1 Journalpost med journalpostId={} og forsendelseMottakId={} eksisterer allerede i databasen.", storedJournalpost
				.getJournalpostId(), tillegsopplysning);
		return buildResponse(storedJournalpost);
	}

	private void updateJournalpost(Journalpost journalpost) {
		journalpost.setJournalstatus(JournalStatusCode.J);
		journalpost.setJournalposttype(JournalpostTypeCode.I);
		journalpost.setJournalDato(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

		for (JournalpostDokumentInfoRelasjon rel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			rel.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn());
		}
	}

	private JournalforInngaaendeForsendelseResponseTo buildResponse(Journalpost journalpost) {
		JournalforInngaaendeForsendelseResponseTo to = JournalforInngaaendeForsendelseResponseTo.builder()
				.journalpostId(journalpost.getJournalpostId())
				.dokumentInfoIdHoveddokument(journalpost.findHoveddokumentDokumentInfoRelasjon()
						.getDokumentInfo()
						.getDokumentInfoId())
				.build();
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

	private Journalpost findPreviousJournalforing(JournalforInngaaendeForsendelseRequestTo requestTo) {
		if (CollectionUtils.isEmpty(requestTo.getJournalpost().getTilleggsopplysninger())) {
			return null;
		}
		String forsendelseMottakId = requestTo.getJournalpost().getTilleggsopplysninger().get(FORSENDELSE_MOTTAK_ID_KEY);
		if (forsendelseMottakId == null || forsendelseMottakId.isEmpty()) {
			return null;
		}

		Long journalpostId = joarkRepository.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(FORSENDELSE_MOTTAK_ID_KEY, forsendelseMottakId);
		if (journalpostId == null) {
			return null;
		}
		return joarkRepository.findById(journalpostId).orElse(null);
	}

	private String getTillegsopplysning(JournalforInngaaendeForsendelseRequestTo requestTo) {
		return requestTo.getJournalpost()
				.getTilleggsopplysninger() == null ? null : requestTo.getJournalpost()
				.getTilleggsopplysninger()
				.get(FORSENDELSE_MOTTAK_ID_KEY);
	}

}
