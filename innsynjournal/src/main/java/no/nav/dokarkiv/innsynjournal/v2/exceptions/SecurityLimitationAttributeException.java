package no.nav.dokarkiv.innsynjournal.v2.exceptions;

import no.nav.modig.core.context.SubjectHandler;

import java.util.Map;

/**
 * Thrown when access to a resource is denied because of some attribute on that resource.
 * Example: Access to a Journalpost is denied because journalpost.mottakskanal has the wrong value.
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class SecurityLimitationAttributeException extends RuntimeException {

	private static final String DEFAULT_RESOURCE = "ressurs";
	private Long journalpostId;
	private Long dokumentInfoId;
	private Map<String, ?> attributeMap;
	private String loggedOnUser;

	public SecurityLimitationAttributeException(Long journalpostId, Long dokumentInfoId, Map<String, ?> attributeMap) {
		this.journalpostId = journalpostId;
		this.dokumentInfoId = dokumentInfoId;
		this.attributeMap = attributeMap;
		this.loggedOnUser = SubjectHandler.getSubjectHandler().getUid();
	}

	@Override
	public String toString() {
		return "SecurityLimitationAttributeException{" +
				"journalpostId=" + journalpostId +
				", dokumentInfoId=" + dokumentInfoId +
				", attributeMap=" + attributeMap +
				", loggedOnUser='" + loggedOnUser + '\'' +
				'}';
	}

	public String toLogMessage() {
		return toLogMessage(DEFAULT_RESOURCE);
	}

	public String toLogMessage(String resource) {
		return "Access denied: Tilgang til " + resource + " med journalpostId=" + journalpostId + " og dokumentInfoId=" + dokumentInfoId +
				" ble ikke gitt. Bruker=" + loggedOnUser + " fikk ikke tilgang pga:" + attributeMap;
	}
}
