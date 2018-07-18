package no.nav.dokarkiv.behandlejournal.v2;

import no.nav.dokarkiv.core.exceptions.AbstractJournalFaultInfoPopulator;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.feil.ForretningsmessigUnntak;
import org.springframework.stereotype.Component;

/**
 * Implementation of BehandleJournalFaultInfoPopulator
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 * 
 */
@Component
public class DefaultBehandleJournalFaultInfoPopulator extends AbstractJournalFaultInfoPopulator implements
		BehandleJournalFaultInfoPopulator {

	/** {@inheritDoc} */
	@Override
	public <T extends ForretningsmessigUnntak> T populateFaultInfo(T faultInfo, Exception exception,
			String operationName) {
		faultInfo.setFeilaarsak(getRootCause(exception).toString());
		faultInfo.setFeilkilde(getErrorSource(operationName));
		faultInfo.setFeilmelding(exception.getMessage());
		faultInfo.setTidspunkt(getXmlTimestamp());
		return faultInfo;
	}
}
