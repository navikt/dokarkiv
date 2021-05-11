package no.nav.dokarkiv.behandlejournal.v2.tjoark060;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Request object for the ArkiverUstrukturertKrav service.
 * 
 * @author Rune Romundstad, Visma Consulting
 *
 */
public class ArkiverUstrukturertKravRequest {

	private final Journalpost journalpost;

	/**
	 * Constructs a new ArkiverUstrukturertKravRequest.
	 *
	 * @param journalpost The Journalpost
	 */
	public ArkiverUstrukturertKravRequest(Journalpost journalpost) {
		this.journalpost = journalpost;
	}
	
	/**
	 * Validate that a Journalpost is set in the request.
	 */
	public void validate() {
		if (journalpost == null) {
			throw new ApplicationException("Journalpost must be set");
		}
	}

	/**
	 * Getter for the journalpost property.
	 *
	 * @return the journalpost
	 */
	public Journalpost getJournalpost() {
		return journalpost;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("journalpost", journalpost)
			.toString();
	}
}
