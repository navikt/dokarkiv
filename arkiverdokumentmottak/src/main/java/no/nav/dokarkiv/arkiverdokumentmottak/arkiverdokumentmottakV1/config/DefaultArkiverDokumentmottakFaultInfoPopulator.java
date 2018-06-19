package no.nav.dokarkiv.arkiverdokumentmottak.arkiverdokumentmottakV1.config;

import no.nav.dokarkiv.arkiverdokumentmottak.AbstractJournalFaultInfoPopulator;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.feil.ForretningsmessigUnntak;

/**
 * Implementation of ArkiverDokumentmottakFaultInfoPopulator.
 *
 * @author Stig Strøm
 */
public class DefaultArkiverDokumentmottakFaultInfoPopulator extends AbstractJournalFaultInfoPopulator {

	public <T extends ForretningsmessigUnntak> T populateFaultInfo(
			T faultInfo, Exception exception, String operationName) {
		faultInfo.setFeilaarsak(getRootCause(exception).toString());
		faultInfo.setFeilkilde(getErrorSource(operationName));
		faultInfo.setFeilmelding(exception.getMessage());
		faultInfo.setTidspunkt(getXmlTimestamp());
		return faultInfo;
	}

}
