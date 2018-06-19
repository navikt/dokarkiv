package no.nav.dokarkiv.arkiverdokumentmottak.arkiverdokumentmottakV1;


import no.nav.dokarkiv.core.domain.entities.Journalpost;

/**
 * Validates the input for JournalforInngaaendeForsendelse
 *
 * @author Stig Strøm
 */
public interface JournalforInngaaendeForsendelseValidator {

	void validate(final Journalpost journalpost, boolean validateStructure);
}
