package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import no.nav.dokarkiv.arkiverdokumentmottak.AbstractJournalFaultInfoPopulator;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.feil.ForretningsmessigUnntak;
import org.springframework.stereotype.Component;

/**
 * Implementation of ArkiverDokumentmottakV2FaultInfoPopulator (TJOARK203)
 *
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class ArkiverDokumentmottakV2FaultInfoPopulator extends AbstractJournalFaultInfoPopulator {

	public <T extends ForretningsmessigUnntak> T populateFaultInfo(
			T faultInfo, Exception exception, String operationName) {
		faultInfo.setFeilaarsak(getRootCause(exception).toString());
		faultInfo.setFeilkilde(getErrorSource(operationName));
		faultInfo.setFeilmelding(exception.getMessage());
		faultInfo.setTidspunkt(getXmlTimestamp());
		return faultInfo;
	}
}
