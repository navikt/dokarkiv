package no.nav.dokarkiv.arkiverdokumentmottak.v1.tjoark203;


import no.nav.dokarkiv.core.domain.entities.Journalpost;

/**
 * Validates the input for JournalforInngaaendeForsendelse
 *
 * @author Stig Str�m
 */
public interface JournalforInngaaendeForsendelseValidator {

	void validate(final Journalpost journalpost, boolean validateStructure);
}
