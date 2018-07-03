package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.ArkiverDokumentmottakV2;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.KanIkkeJournalfores;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.meldinger.JournalforInngaaendeForsendelseRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.meldinger.JournalforInngaaendeForsendelseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Provider class for JournalforInngaaendeForsendelseV2 (TJOARK203)
 *
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class ArkiverDokumentmottakV2Provider implements ArkiverDokumentmottakV2 {

	private static final Logger log = LoggerFactory.getLogger(ArkiverDokumentmottakV2Provider.class);

	private static final String ARKIVER_DOKUMENTMOTTAK_V2 = "provider.arkiverDokumentmottak.v2";

	private static final String JOURNALFOR_INNGAAENDE_FORSENDELSE_V2 = ARKIVER_DOKUMENTMOTTAK_V2 + ".journalforInngaaendeForsendelse";
	private static final String PING = ARKIVER_DOKUMENTMOTTAK_V2 + ".ping";

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
//	@Timed(name = JOURNALFOR_INNGAAENDE_FORSENDELSE_V2 + ".timer", absolute = true)
//	@Counted(name = JOURNALFOR_INNGAAENDE_FORSENDELSE_V2, absolute = true, monotonic = true)
//	@Metered(name = JOURNALFOR_INNGAAENDE_FORSENDELSE_V2 + ".meter", absolute = true)
//	@ExceptionMetered(name = JOURNALFOR_INNGAAENDE_FORSENDELSE_V2 + ".exceptionMeter", absolute = true)
	public JournalforInngaaendeForsendelseResponse journalforInngaaendeForsendelse(
			JournalforInngaaendeForsendelseRequest request) throws KanIkkeJournalfores {

		JournalforInngaaendeForsendelseV2RequestTo requestTo;
		JournalforInngaaendeForsendelseV2ResponseTo responseTo;

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
	@Transactional
//	@Counted(name = PING + ".counter", absolute = true, monotonic = true)
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
}
