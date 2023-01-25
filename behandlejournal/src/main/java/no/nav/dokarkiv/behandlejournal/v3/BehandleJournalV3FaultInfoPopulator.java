package no.nav.dokarkiv.behandlejournal.v3;

import no.nav.dokarkiv.core.exceptions.AbstractJournalFaultInfoPopulator;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.feil.ForretningsmessigUnntak;
import org.springframework.stereotype.Component;

/**
 * Implementation of BehandleJournalFaultInfoPopulator
 */
@Component
public class BehandleJournalV3FaultInfoPopulator extends AbstractJournalFaultInfoPopulator {

	public <T extends ForretningsmessigUnntak> T populateFaultInfo(T faultInfo, Exception exception,
			String operationName) {
		faultInfo.setFeilaarsak(getRootCause(exception).toString());
		faultInfo.setFeilkilde(getErrorSource(operationName));
		faultInfo.setFeilmelding(exception.getMessage());
		faultInfo.setTidspunkt(getXmlTimestamp());
		return faultInfo;
	}
}
