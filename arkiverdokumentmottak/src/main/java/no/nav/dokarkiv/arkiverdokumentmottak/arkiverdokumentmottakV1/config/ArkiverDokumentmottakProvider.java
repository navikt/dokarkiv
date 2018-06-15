package no.nav.dokarkiv.arkiverdokumentmottak.arkiverdokumentmottakV1.config;

import static no.nav.service.dok.joark.ServiceConstants.FORSENDELSE_MOTTAK_ID_KEY;

import com.google.common.base.Objects;
import no.nav.dokarkiv.arkiverdokumentmottak.arkiverdokumentmottakV1.DefaultJournalforInngaaendeForsendelseRequestMapper;
import no.nav.dokarkiv.arkiverdokumentmottak.arkiverdokumentmottakV1.DefaultJournalforInngaaendeForsendelseResponseMapper;
import no.nav.dokarkiv.arkiverdokumentmottak.arkiverdokumentmottakV1.DefaultJournalforInngaaendeForsendelseService;
import no.nav.dokarkiv.arkiverdokumentmottak.arkiverdokumentmottakV1.JournalforInngaaendeForsendelseRequestTo;
import no.nav.dokarkiv.arkiverdokumentmottak.arkiverdokumentmottakV1.JournalforInngaaendeForsendelseResponseTo;
import no.nav.dokarkiv.core.domain.DefaultPingService;
import no.nav.dokarkiv.core.exceptions.FunctionalUnrecoverableException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.ArkiverDokumentmottakV1;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.KanIkkeJournalfores;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.Tilleggsopplysning;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;


public class ArkiverDokumentmottakProvider implements ArkiverDokumentmottakV1 {
	private static final Logger log = LoggerFactory.getLogger(ArkiverDokumentmottakProvider.class);

	private static final String ARKIVER_DOKUMENTMOTTAK_V1 = "provider.arkiverDokumentmottak.v1";

	private static final String JOURNALFOR_INNGAAENDE_FORSENDELSE = ARKIVER_DOKUMENTMOTTAK_V1 + ".journalforInngaaendeForsendelse";
	private static final String PING = ARKIVER_DOKUMENTMOTTAK_V1 + ".ping";

	@Inject
	private DefaultPingService pingService;

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
	@Transactional
	//FIXME Her ble codahale@Counted annotering brukt. Legg til tilsvarende metrikker i form av prometheus og micrometer
	public void ping() {
		pingService.ping();
	}

	private String findTilleggsOpplysning(JournalforInngaaendeForsendelseRequest request) {
		if (request != null && request.getJournalpost() != null && request.getJournalpost()
				.getJournalpostTilleggsopplysninger() != null) {
			for (Tilleggsopplysning tilleggsopplysning : request.getJournalpost().getJournalpostTilleggsopplysninger()) {
				if (tilleggsopplysning != null && FORSENDELSE_MOTTAK_ID_KEY.equals(tilleggsopplysning.getOpplysningsnoekkel())) {
					return Objects.toStringHelper(Tilleggsopplysning.class)
							.add("key", FORSENDELSE_MOTTAK_ID_KEY)
							.add("value", tilleggsopplysning.getOpplysningsverdi()).toString();
				}
			}
		}
		return null;
	}
}