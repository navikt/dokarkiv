package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Exception that is thrown when attempting to set Journalstatus to an illegal value
 * 
 * @author Magnus Skuland, Sirius IT
 */
public class UgyldigJournalStatusVerdiException extends FunctionalRecoverableException {

	/** Unique serial version id. */
	private static final long serialVersionUID = 1L;

	private final JournalStatusCode journalStatus;

	/**
	 * Constructs a new UgyldigJournalStatusVerdiException.
	 * 
	 * @param message
	 *            The exception message.
	 * @param journalStatus
	 *            The invalid status.
	 */
	public UgyldigJournalStatusVerdiException(String message, JournalStatusCode journalStatus) {
		super(message);
		this.journalStatus = journalStatus;
	}

	/**
	 * Getter for the journalStatus property.
	 *
	 * @return the journalStatus
	 */
	public JournalStatusCode getJournalStatus() {
		return journalStatus;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.appendSuper(super.toString());
		builder.append("journalStatus", journalStatus);
		return builder.toString();
	}

}
