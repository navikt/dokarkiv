package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class JournalpostIkkeFerdigstiltException extends IllegalJournalStatusException {

    public JournalpostIkkeFerdigstiltException(String message) {
        super(message);
    }
}
