package no.nav.dokarkiv.arkiverdokumentmottak.v1;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.arkiverdokumentmottak.DefaultArkiverDokumentmottakFaultInfoPopulator;
import no.nav.dokarkiv.arkiverdokumentmottak.ServiceConstants;
import no.nav.dokarkiv.arkiverdokumentmottak.v1.tjoark203.DefaultJournalforInngaaendeForsendelseRequestMapper;
import no.nav.dokarkiv.arkiverdokumentmottak.v1.tjoark203.DefaultJournalforInngaaendeForsendelseResponseMapper;
import no.nav.dokarkiv.arkiverdokumentmottak.v1.tjoark203.DefaultJournalforInngaaendeForsendelseService;
import no.nav.dokarkiv.arkiverdokumentmottak.v1.to.JournalforInngaaendeForsendelseRequestTo;
import no.nav.dokarkiv.arkiverdokumentmottak.v1.to.JournalforInngaaendeForsendelseResponseTo;
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
	@Inject
	private DefaultJournalforInngaaendeForsendelseResponseMapper journalforInngaaendeForsendelseResponseMapper;

	@Inject
	private DefaultJournalforInngaaendeForsendelseRequestMapper journalforInngaaendeForsendelseRequestMapper;

	@Inject
	private DefaultJournalforInngaaendeForsendelseService journalforInngaaendeForsendelseService;

	@Inject
	private DefaultArkiverDokumentmottakFaultInfoPopulator faultInfoPopulator;

	@Override
	@Transactional
	//FIXME Her ble codahale @Timed @Counted @Metered @ExceptionMetered annoteringer brukt. Legg til tilsvarende metrikker i form av prometheus og micrometer
	public JournalforInngaaendeForsendelseResponse journalforInngaaendeForsendelse(
			JournalforInngaaendeForsendelseRequest request) throws KanIkkeJournalfores {
		JournalforInngaaendeForsendelseRequestTo requestTo = journalforInngaaendeForsendelseRequestMapper.map(request);

		JournalforInngaaendeForsendelseResponseTo responseTo;
		try {
			responseTo = journalforInngaaendeForsendelseService.journalforInngaaendeForsendelse(requestTo);
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
					return "(key=" + ServiceConstants.FORSENDELSE_MOTTAK_ID_KEY + " value=" + tilleggsopplysning.getOpplysningsverdi();
					//FIXME Sjekk originalen. Alternative metode istedenfor å bruke Guava
				}
			}
		}
		return null;
	}
}