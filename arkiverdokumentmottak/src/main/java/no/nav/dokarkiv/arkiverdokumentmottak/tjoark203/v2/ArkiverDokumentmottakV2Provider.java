package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.ArkiverDokumentmottakV2;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.KanIkkeJournalfores;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.meldinger.JournalforInngaaendeForsendelseRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.meldinger.JournalforInngaaendeForsendelseResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Provider class for JournalforInngaaendeForsendelseV2 (TJOARK203)
 *
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
@Slf4j
public class ArkiverDokumentmottakV2Provider implements ArkiverDokumentmottakV2 {

	@Inject
	private JournalforInngaaendeForsendelseV2ResponseMapper journalforInngaaendeForsendelseV2ResponseMapper;

	@Inject
	private JournalforInngaaendeForsendelseV2RequestMapper journalforInngaaendeForsendelseV2RequestMapper;

	@Inject
	private JournalforInngaaendeForsendelseV2Service journalforInngaaendeForsendelseV2Service;

	@Inject
	private ArkiverDokumentmottakV2FaultInfoPopulator faultInfoPopulator;

	@Override
	@Transactional
	public JournalforInngaaendeForsendelseResponse journalforInngaaendeForsendelse(
			JournalforInngaaendeForsendelseRequest request) throws KanIkkeJournalfores {

		log.info("TJOARK203_V2 har mottatt forsendelse med kanalreferanseId={} og mottakskanal={}.", getKanalereferanseId(request), getMottakskanal(request));

		try {
			JournalforInngaaendeForsendelseV2RequestTo requestTo = journalforInngaaendeForsendelseV2RequestMapper.map(request);
			log.info("TJOARK203_V2 har mappet om forsendelse med kanalreferanseId={} og mottakskanal={} til to objekt og er klar til å journalføre.", getKanalereferanseId(request), getMottakskanal(request));

			JournalforInngaaendeForsendelseV2ResponseTo responseTo = journalforInngaaendeForsendelseV2Service.journalforInngaaendeForsendelseV2(requestTo);
			return journalforInngaaendeForsendelseV2ResponseMapper.map(responseTo);
		} catch (InvalidArgumentException | InvalidJournalpostStructureException | IllegalArgumentException e) {
			log.warn(String.format("TJOARK203_V2 Kan ikke journalføre inngående forsendelse. Feilmelding=%s. %s", e.getMessage(), getAdditionalErrorInfo(request)), e);
			throw new KanIkkeJournalfores(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.feil.KanIkkeJournalfores(), e, "journalforInngaaendeForsendelseV2"));
		}

	}

	@Override
	public void ping() {
		//noop
	}

	private String getAdditionalErrorInfo(JournalforInngaaendeForsendelseRequest request) {
		StringBuilder sb = new StringBuilder();
		if (request != null && request.getJournalpost() != null) {
			sb.append("KanalreferanseId=" + request.getJournalpost().getKanalReferanseId());
			sb.append(", Mottakskanal=" + request.getJournalpost().getMottakskanal());
		}
		return sb.toString();
	}

	private String getKanalereferanseId(JournalforInngaaendeForsendelseRequest request) {
		if (request == null || request.getJournalpost() == null) {
			return null;
		}

		return request.getJournalpost().getKanalReferanseId();
	}

	private String getMottakskanal(JournalforInngaaendeForsendelseRequest request) {
		if (request == null || request.getJournalpost() == null) {
			return null;
		}

		return request.getJournalpost().getMottakskanal();
	}
}
