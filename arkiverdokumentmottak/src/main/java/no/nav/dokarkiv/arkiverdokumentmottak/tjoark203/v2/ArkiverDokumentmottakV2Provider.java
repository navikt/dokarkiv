package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import io.micrometer.core.annotation.Timed;
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
	public JournalforInngaaendeForsendelseV2ResponseMapper journalforInngaaendeForsendelseV2ResponseMapper;

	@Inject
	public JournalforInngaaendeForsendelseV2RequestMapper journalforInngaaendeForsendelseV2RequestMapper;

	@Inject
	public JournalforInngaaendeForsendelseV2Service journalforInngaaendeForsendelseV2Service;

	@Inject
	public ArkiverDokumentmottakV2FaultInfoPopulator faultInfoPopulator;

	@Override
	@Transactional
	@Timed(value = "dok_request", extraTags = {"process_code", "TJOARK203_V2"}, percentiles = {0.5, 0.95})
	public JournalforInngaaendeForsendelseResponse journalforInngaaendeForsendelse(
			JournalforInngaaendeForsendelseRequest request) throws KanIkkeJournalfores {

		JournalforInngaaendeForsendelseV2RequestTo requestTo;
		JournalforInngaaendeForsendelseV2ResponseTo responseTo;

		log.info("TJOARK203_V2 har mottat forsendelse med kanalreferanseId={} og mottakskanal={}.", getKanalereferanseId(request), getMottakskanal(request));

		try {
			requestTo = journalforInngaaendeForsendelseV2RequestMapper.map(request);
			responseTo = journalforInngaaendeForsendelseV2Service.journalforInngaaendeForsendelseV2(requestTo);
		} catch (InvalidArgumentException | InvalidJournalpostStructureException | IllegalArgumentException e) {
			throw new KanIkkeJournalfores(e.getMessage() + getAdditionalErrorInfo(request), faultInfoPopulator.populateFaultInfo(
					new no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.feil.KanIkkeJournalfores(), e, "journalforInngaaendeForsendelseV2"));
		}

		return journalforInngaaendeForsendelseV2ResponseMapper.map(responseTo);
	}

	@Override
	public void ping() {
		//noop
	}

	private String getAdditionalErrorInfo(JournalforInngaaendeForsendelseRequest request) {
		StringBuilder sb = new StringBuilder(". ");
		if (request != null && request.getJournalpost() != null) {
			sb.append("KanalreferanseId=" + request.getJournalpost().getKanalReferanseId());
			sb.append(", Mottakskanal=" + request.getJournalpost().getMottakskanal());
		}
		return sb.toString();
	}

	private String getKanalereferanseId(JournalforInngaaendeForsendelseRequest request) {
		if (request.getJournalpost() == null) {
			return null;
		}

		return request.getJournalpost().getKanalReferanseId();
	}

	private String getMottakskanal(JournalforInngaaendeForsendelseRequest request) {
		if (request.getJournalpost() == null) {
			return null;
		}

		return request.getJournalpost().getMottakskanal();
	}
}
