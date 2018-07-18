package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Request object for operation OpprettJournalpost method in the
 * arkiverDokumentproduksjon
 *
 * @author Stig Strøm
 */

public class OpprettJournalpostRequestTo {
	private Journalpost journalpost;

	/**
	 * Default constructor only used for mapping.
	 */
	@SuppressWarnings("unused")
	private OpprettJournalpostRequestTo() {
	}

	/**
	 * Constructor taking request fields as parameter.
	 *
	 * @param journalpost The Journalpost object in request.
	 */
	public OpprettJournalpostRequestTo(Journalpost journalpost) {
		this.journalpost = journalpost;
	}

	/**
	 * Check that journalpost is set.
	 */
	public void validate() {
		if (journalpost == null) {
			throw new ApplicationException("Missing parameter in request: Journalpost");
		}

		if (journalpost.findHoveddokumentDokumentInfoRelasjon() == null) {
			throw new ApplicationException("Missing parameter in request: Hoveddokument");
		}
	}

	/**
	 * Getter for the Journalpost property.
	 *
	 * @return the Journalpost object.
	 */
	public Journalpost getJournalpost() {
		return journalpost;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("journalpost", journalpost)
				.toString();
	}
}
