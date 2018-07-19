package no.nav.dokarkiv.arkiverdokumentproduksjon;

import no.nav.dokarkiv.core.exceptions.AbstractJournalFaultInfoPopulator;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.ForretningsmessigUnntak;
import org.springframework.stereotype.Component;

/**
 * Implementation of ArkiverDokumentproduksjonFaultInfoPopulator.
 *
 * @author Stig Strøm
 */
@Component
public class DefaultArkiverDokumentproduksjonFaultInfoPopulator extends AbstractJournalFaultInfoPopulator implements
		ArkiverDokumentproduksjonFaultInfoPopulator {

	@Override
	public <T extends ForretningsmessigUnntak> T populateFaultInfo(
			T faultInfo, Exception exception, String operationName) {
		faultInfo.setFeilaarsak(getRootCause(exception).toString());
		faultInfo.setFeilkilde(getErrorSource(operationName));
		faultInfo.setFeilmelding(exception.getMessage());
		faultInfo.setTidspunkt(getXmlTimestamp());
		return faultInfo;
	}
}
