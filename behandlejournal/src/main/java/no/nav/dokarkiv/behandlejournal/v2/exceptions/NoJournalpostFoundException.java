package no.nav.dokarkiv.behandlejournal.v2.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Thrown when no <code>Journalpost</code> was retrieved for given journalpostId.
 * <p>
 * FeilID: JOARKV15
 *
 * @author Magnus Skuland, Sirius IT
 */
public class NoJournalpostFoundException extends FunctionalRecoverableException {

	/**
	 * Id used for serialization.
	 */
	private static final long serialVersionUID = 1L;

	private final Long journalpostId;

	/**
	 * Constructs a new NoJournalpostFoundException.
	 *
	 * @param message       The exception message.
	 * @param journalpostId The journalpostId.
	 */
	public NoJournalpostFoundException(String message, Long journalpostId) {
		super(message);
		this.journalpostId = journalpostId;
	}

	/**
	 * Getter for journalpostId property.
	 *
	 * @return The journalpostId.
	 */
	public Long getJournalpostId() {
		return journalpostId;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.appendSuper(super.toString());
		builder.append("journalpostId", journalpostId);
		return builder.toString();
	}
}
