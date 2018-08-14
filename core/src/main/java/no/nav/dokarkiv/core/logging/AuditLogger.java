package no.nav.dokarkiv.core.logging;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;

/**
 * Utility class for audit logging.
 *
 * @author Carl-Henrik Lund, Bekk
 */
@Slf4j(topic = "auditLogger")
public final class AuditLogger {
	private AuditLogger() {
	}
	
	/**
	 * This method creates an AuditItem and calls the info method.
	 * 
	 * @param methodName The name of the method to be invoked
	 * @param journalpost The journalpost object just for populating log information
	 * @param filDetaljer The FilDetaljer describing the fil being read.
	 */
	public static void generateAuditLog(String methodName, Journalpost journalpost, FilDetaljer filDetaljer) {
		if(log.isInfoEnabled()) {
			AuditItem auditItem = new AuditItem();
			auditItem.setMessage("Calling " + methodName + "...");
			auditItem.setSource(RequestContextHolder.currentRequestContext().getComponentId());
			auditItem.setTransactionId(RequestContextHolder.currentRequestContext().getTransactionId());
			auditItem.setUserId(RequestContextHolder.currentRequestContext().getUserId());
			auditItem.setTarget("JOARK");
			auditItem.setProtectionLevel(AuditItem.ProtectionLevel.HIGH);
			auditItem.setAccessType(AuditItem.AccessType.AUDIT_READ);
			auditItem.addCustomInfo("ScreenId", RequestContextHolder.currentRequestContext().getScreenId());
			if (journalpost.getLestDato() != null) {
				auditItem.addCustomInfo("Journalpost.datoLest", journalpost.getLestDato().toString());
			}
			auditItem.addCustomInfo("FilDetaljer.filnavn", filDetaljer.getFilnavn());
			if (journalpost.getBrukere().iterator().hasNext()) {
				Bruker gjelderInfo = journalpost.getBrukere().iterator().next();
				if (gjelderInfo != null) {
					auditItem.addCustomInfo("Journalpost.Bruker.brukerID", gjelderInfo.getBrukerId());
				}
			}
			log.info("{}", auditItem);
		}
	}
}