package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.arkiverdokumentmottak.ArkiverDokumentmottakConstants;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.ArkiverDokumentmottakV1;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.KanIkkeJournalfores;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.Tilleggsopplysning;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Component
@Slf4j
public class ArkiverDokumentmottakProvider implements ArkiverDokumentmottakV1 {

	@Inject
	private JournalforInngaaendeForsendelseResponseMapper journalforInngaaendeForsendelseResponseMapper;

	@Inject
	private JournalforInngaaendeForsendelseRequestMapper journalforInngaaendeForsendelseRequestMapper;

	@Inject
	private JournalforInngaaendeForsendelseService journalforInngaaendeForsendelseService;

	@Inject
	private ArkiverDokumentmottakFaultInfoPopulator faultInfoPopulator;

	@Override
	@Transactional
	public JournalforInngaaendeForsendelseResponse journalforInngaaendeForsendelse(
			JournalforInngaaendeForsendelseRequest request) throws KanIkkeJournalfores {

		String tillegsopplysning = findTilleggsOpplysningForsendelseMottakId(request);
		log.info("TJOARK203_V1 har mottatt forsendelse med tilleggsopplysning.ForsendelseMottakId={}", tillegsopplysning);

		JournalforInngaaendeForsendelseRequestTo requestTo = journalforInngaaendeForsendelseRequestMapper.map(request);
		log.info("TJOARK203_V1 har mappet om forsendelse til to objekt og er klar til å journalføre. ", tillegsopplysning);
		try {
			JournalforInngaaendeForsendelseResponseTo responseTo = journalforInngaaendeForsendelseService.journalforInngaaendeForsendelse(requestTo);
			return journalforInngaaendeForsendelseResponseMapper.map(responseTo);
		} catch (DokarkivFunctionalException | IllegalArgumentException e) {
			String tilleggsOpplysning = findTilleggsOpplysningForsendelseMottakId(request);
			log.warn(String.format("Kan ikke journalføre inngående forsendelse. feilmelding=%s, tilleggsopplysning.forsendelseMottakId=%s", e
					.getMessage(), tilleggsOpplysning), e);
			throw new KanIkkeJournalfores(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.feil.KanIkkeJournalfores(), e, "journalforInngaaendeForsendelse"));
		}
	}

	@Override
	public void ping() {
		//noop
	}

	private String findTilleggsOpplysningForsendelseMottakId(JournalforInngaaendeForsendelseRequest request) {
		if (request != null && request.getJournalpost() != null && request.getJournalpost()
				.getJournalpostTilleggsopplysninger() != null) {
			for (Tilleggsopplysning tilleggsopplysning : request.getJournalpost().getJournalpostTilleggsopplysninger()) {
				if (tilleggsopplysning != null && ArkiverDokumentmottakConstants.FORSENDELSE_MOTTAK_ID_KEY.equals(tilleggsopplysning.getOpplysningsnoekkel())) {
					return tilleggsopplysning.getOpplysningsverdi();
				}
			}
		}
		return null;
	}
}