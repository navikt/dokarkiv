package no.nav.provider.dok.joark.nsb;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.ForretningsmessigUnntak;

/**
 * Used to populate ForretningsmessigUnntak faultInfos in checked exceptions.
 * 
 * @author Stig Strøm
 */
public interface ArkiverDokumentproduksjonFaultInfoPopulator {

	/**
	 * Populates a faultInfo.
	 * 
	 * @param faultInfo The faultInfo to populate.
	 * @param exception The thrown exception.
	 * @param operationName The name of the operation that threw the exception.
	 * @return The populated faultInfo
	 */
	<T extends ForretningsmessigUnntak> T populateFaultInfo(T faultInfo, Exception exception, String operationName);
	
}
