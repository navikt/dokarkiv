package no.nav.dokarkiv.core.audit;

import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for audit logging.
 *
 * @author Carl-Henrik Lund, Bekk
 */
public final class AuditLogger {
	public static final Logger auditLogger = LoggerFactory.getLogger("auditLogger");
	/**
	 * Avoid instantiation.
	 */
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
		auditLogger.info("{}", auditItem);
	}

}