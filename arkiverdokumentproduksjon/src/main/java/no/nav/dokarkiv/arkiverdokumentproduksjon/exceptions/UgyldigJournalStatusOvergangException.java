package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Exception that is thrown when there is an illegal journalstatus transition.
 *
 * @author Andreas Johansson, Bekk Consulting
 */
public class UgyldigJournalStatusOvergangException extends DokarkivFunctionalException {

	/**
	 * Unique serial version id.
	 */
	private static final long serialVersionUID = ***gammelt_fnr***52808018L;

	private final JournalStatusCode existingJournalStatus;
	private final JournalStatusCode updatedJournalStatus;
	private final JournalpostTypeCode journalpostType;

	/**
	 * Constructs a new UgyldigJournalStatusOvergangException.
	 *
	 * @param message               The exception message.
	 * @param existingJournalStatus The old/existing JournalpostStatus
	 * @param updatedJournalStatus  The new/updated JournalpostStatus
	 * @param journalpostType       The JournalpostType for which the transition rules will be
	 *                              applied
	 */
	public UgyldigJournalStatusOvergangException(String message, JournalStatusCode existingJournalStatus,
												 JournalStatusCode updatedJournalStatus, JournalpostTypeCode journalpostType) {
		super(message);
		this.existingJournalStatus = existingJournalStatus;
		this.updatedJournalStatus = updatedJournalStatus;
		this.journalpostType = journalpostType;
	}

	/**
	 * Getter for the existingJournalStatus property.
	 *
	 * @return the existingJournalStatus
	 */
	public JournalStatusCode getExistingJournalStatus() {
		return existingJournalStatus;
	}

	/**
	 * Getter for the updatedJournalStatus property.
	 *
	 * @return the updatedJournalStatus
	 */
	public JournalStatusCode getUpdatedJournalStatus() {
		return updatedJournalStatus;
	}

	/**
	 * Getter for the journalpostType property.
	 *
	 * @return the journalpostType
	 */
	public JournalpostTypeCode getJournalpostType() {
		return journalpostType;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.appendSuper(super.toString());
		builder.append("existingJournalStatus", existingJournalStatus);
		builder.append("updatedJournalStatus", updatedJournalStatus);
		builder.append("journalpostType", journalpostType);
		return builder.toString();
	}

}
