package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;


import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;

/**
 * Request object for operation OpprettOgFerdigstillJournalpost.
 *
 * @author Stig Strøm
 */
@Data
@Builder
@NoArgsConstructor
public class OpprettJournalpostArkiverDokumenterRequestTo {
	private Journalpost journalpost;

	/**
	 * Constructor taking request fields as parameter.
	 *
	 * @param journalpost The Journalpost object in request.
	 */
	public OpprettJournalpostArkiverDokumenterRequestTo(Journalpost journalpost) {
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

	}
}
