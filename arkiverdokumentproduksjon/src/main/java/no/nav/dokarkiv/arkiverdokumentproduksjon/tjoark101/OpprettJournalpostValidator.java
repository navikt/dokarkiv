package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import no.nav.dokarkiv.core.domain.entities.Journalpost;

/**
 * Validator for the OpprettJournalpost method in
 * arkiverDokumentproduksjon service
 *
 * @author Stig Strøm
 */
public interface OpprettJournalpostValidator {

	void validate(Journalpost journalpost);

}
