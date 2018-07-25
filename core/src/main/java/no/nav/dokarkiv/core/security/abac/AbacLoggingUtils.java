package no.nav.dokarkiv.core.security.abac;

import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.freg.abac.core.dto.response.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Martin Burheim Tingstad, Visma Consulting AS
 */
@Component
public class AbacLoggingUtils {
	private static final Logger ABAC_LOG = LoggerFactory.getLogger("abac");

	private static final String NO_ACCESS_TO_JOURNALPOST = "Refused access to journalpostId=";
	private static final String ACCESS_TO_MARKED_JOURNALPOST = "Granted access to marked journalpostId=";
	private static final String REQUESTED_ACCESS_TO_JOURNALPOST_WITH_MULTIPLE_USERS
			= "Requested access to journalpost with multiple users, journalpostId=";

	private static final String NO_ACCESS_TO_SAK = "Refused access to sak with sakId=";
	private static final String ACCESS_TO_MARKED_SAK = "Granted access to marked sak with sakid=";

	public void logAccessDeniedToJournalpost(Long journalpostId) {
		ABAC_LOG.warn(String.format("%s%d", NO_ACCESS_TO_JOURNALPOST, journalpostId));
	}

	public void logAccessToJournalpostWithAdvice(Long journalpostId) {
		ABAC_LOG.warn(String.format("%s%d", ACCESS_TO_MARKED_JOURNALPOST, journalpostId));
	}

	public void logAccessToJournalpostWithSeveralUsers(Long journalpostId) {
		ABAC_LOG.warn(String.format("%s%d", REQUESTED_ACCESS_TO_JOURNALPOST_WITH_MULTIPLE_USERS, journalpostId));
	}

	public void logAttemptedAccessToSak(String sakId, FagsystemCode fagsystemCode) {
		ABAC_LOG.warn(String.format("%s%s (fagsystem=%s)", NO_ACCESS_TO_SAK, sakId, fagsystemCode.name()));
	}

	public void logAccessToSakdWithAdvice(String sakId, FagsystemCode fagsystemCode) {
		ABAC_LOG.warn(String.format("%s%s (fagsystem=%s)", ACCESS_TO_MARKED_SAK, sakId, fagsystemCode.name()));
	}

	public void logAdvice(Advice advice) {
		ABAC_LOG.warn(advice.toString());
	}

}
