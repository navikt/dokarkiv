package no.nav.dokarkiv.behandlejournal.v3;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.feil.ForretningsmessigUnntak;

/**
 * Used to populate ForretningsmessigUnntak faultInfos in checked exceptions.
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 *
 */
public interface BehandleJournalFaultInfoPopulator {

	/**
	 * Populates a faultInfo.
	 * 
	 * @param <T> A subtype of ForretningsmessigUnntak.
	 * @param faultInfo The faultInfo to populate.
	 * @param exception The thrown exception.
	 * @param operationName The name of the operation that threw the exception.
	 * @return The populated faultInfo
	 */
	<T extends ForretningsmessigUnntak> T populateFaultInfo(T faultInfo, Exception exception, String operationName);

}