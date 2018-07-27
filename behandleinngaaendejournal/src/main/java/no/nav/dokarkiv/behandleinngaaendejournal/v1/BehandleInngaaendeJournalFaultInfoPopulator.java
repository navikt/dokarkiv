package no.nav.dokarkiv.behandleinngaaendejournal.v1;

import no.nav.dokarkiv.core.exceptions.AbstractJournalFaultInfoPopulator;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.feil.ForretningsmessigUnntak;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class BehandleInngaaendeJournalFaultInfoPopulator extends AbstractJournalFaultInfoPopulator {
	public <T extends ForretningsmessigUnntak> T populateFaultInfo(T faultInfo, Exception exception,
																   String operationName) {
		faultInfo.setFeilaarsak(getRootCause(exception).toString());
		faultInfo.setFeilkilde(getErrorSource(operationName));
		faultInfo.setFeilmelding(exception.getMessage());
		faultInfo.setTidspunkt(getXmlTimestamp());
		return faultInfo;
	}
}
