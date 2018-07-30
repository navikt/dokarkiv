package no.nav.dokarkiv.journal.v3;

import no.nav.dokarkiv.core.exceptions.AbstractJournalFaultInfoPopulator;
import no.nav.tjeneste.virksomhet.journal.v3.feil.ForretningsmessigUnntak;

/**
 * Implementation of JournalV3FaultInfoPopulator.
 *
 * @author Stig Strøm
 */
public class DefaultJournalV3FaultInfoPopulator extends AbstractJournalFaultInfoPopulator implements
		JournalV3FaultInfoPopulator {

	@Override
	public <T extends ForretningsmessigUnntak> T populateFaultInfo(T faultInfo, Exception exception, String operationName) {
		faultInfo.setFeilaarsak(getRootCause(exception).toString());
		faultInfo.setFeilkilde(getErrorSource(operationName));
		faultInfo.setFeilmelding(exception.getMessage());
		faultInfo.setTidspunkt(getXmlTimestamp());
		return faultInfo;
	}
	
}
