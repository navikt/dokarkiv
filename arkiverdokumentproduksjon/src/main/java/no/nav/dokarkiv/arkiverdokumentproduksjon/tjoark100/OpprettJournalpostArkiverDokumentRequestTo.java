package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;


import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ApplicationException;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Request object for operation OpprettOgFerdigstillJournalpost.
 *
 * @author Stig Strøm
 */

public class OpprettJournalpostArkiverDokumentRequestTo {
	private Journalpost journalpost;
	private boolean ferdigstillJournalpost;

	/**
	 * Default constructor only used for mapping.
	 */
	@SuppressWarnings("unused")
	private OpprettJournalpostArkiverDokumentRequestTo() {
	}

	/**
	 * Constructor taking request fields as parameter.
	 *
	 * @param journalpost The Journalpost object in request.
	 * @param ferdigstillJournalpost whether the Journalpost should be ferdigstillt
	 */
	public OpprettJournalpostArkiverDokumentRequestTo(Journalpost journalpost, boolean ferdigstillJournalpost) {
		this.ferdigstillJournalpost = ferdigstillJournalpost;
		this.journalpost = journalpost;
		validate();
	}

	/**
	 * Check that journalpost is set.
	 */
	private void validate() {
		if (journalpost == null) {
			throw new ApplicationException("Missing parameter in request: Journalpost");
		}

		if (journalpost.findHoveddokumentDokumentInfoRelasjon() == null) {
			throw new ApplicationException("Missing parameter in request: Hoveddokument");
		}

		if (ferdigstillJournalpost && (journalpost.getJournalposttype() == JournalpostTypeCode.U || journalpost.getJournalposttype() == null)
				&& journalpost.getUtsendingskanal() == null) {
			throw new ApplicationException("Missing parameter in request: Utsendingskanal");
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

	public boolean isFerdigstillJournalpost() {
		return ferdigstillJournalpost;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("journalpost", journalpost)
				.toString();
	}
}
