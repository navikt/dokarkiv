package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.arkiverdokumentmottak.ServiceConstants;
import no.nav.dokarkiv.core.stelvio.FunctionalUnrecoverableException;
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

	private static final String ARKIVER_DOKUMENTMOTTAK_V1 = "provider.arkiverDokumentmottak.v1";

	private static final String JOURNALFOR_INNGAAENDE_FORSENDELSE = ARKIVER_DOKUMENTMOTTAK_V1 + ".journalforInngaaendeForsendelse";
	private static final String PING = ARKIVER_DOKUMENTMOTTAK_V1 + ".ping";

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
	//FIXME Her ble codahale @Timed @Counted @Metered @ExceptionMetered annoteringer brukt. Legg til tilsvarende metrikker i form av prometheus og micrometer
	public JournalforInngaaendeForsendelseResponse journalforInngaaendeForsendelse(
			JournalforInngaaendeForsendelseRequest request) throws KanIkkeJournalfores {

		String tillegsopplysning = findTilleggsOpplysning(request);
		log.info("TJOARK203_V1 har mottatt forsendelse med tilleggsopplysning.ForsendelseMottakId={} og mapper om forsendelsen til TO objekt", tillegsopplysning);

		JournalforInngaaendeForsendelseRequestTo requestTo = journalforInngaaendeForsendelseRequestMapper.map(request);

		JournalforInngaaendeForsendelseResponseTo responseTo;
		try {
			responseTo = journalforInngaaendeForsendelseService.journalforInngaaendeForsendelse(requestTo);
			log.info("TJOARK203_V1 har journalført inngående forsendelse med tillegsopplysning.ForsendelseMottakId={}, journalpostId={}, dokumentInfoIdHoveddokument={}", tillegsopplysning, responseTo
					.getJournalpostId(), responseTo.getDokumentInfoIdHoveddokument());

		} catch (FunctionalUnrecoverableException | IllegalArgumentException e) {
			String tilleggsOpplysning = findTilleggsOpplysning(request);
			log.warn("Kan ikke journalføre inngående forsendelse. tilleggsopplysning=" + tilleggsOpplysning, e);
			throw new KanIkkeJournalfores(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.feil.KanIkkeJournalfores(), e, "journalforInngaaendeForsendelse"));
		}
		return journalforInngaaendeForsendelseResponseMapper.map(responseTo);
	}

	@Override
	public void ping() {
		//noop
	}

	private String findTilleggsOpplysning(JournalforInngaaendeForsendelseRequest request) {
		if (request != null && request.getJournalpost() != null && request.getJournalpost()
				.getJournalpostTilleggsopplysninger() != null) {
			for (Tilleggsopplysning tilleggsopplysning : request.getJournalpost().getJournalpostTilleggsopplysninger()) {
				if (tilleggsopplysning != null && ServiceConstants.FORSENDELSE_MOTTAK_ID_KEY.equals(tilleggsopplysning.getOpplysningsnoekkel())) {
					return tilleggsopplysning.getOpplysningsverdi();
					//FIXME Sjekk originalen. Alternative metode istedenfor å bruke Guava
				}
			}
		}
		return null;
	}
}