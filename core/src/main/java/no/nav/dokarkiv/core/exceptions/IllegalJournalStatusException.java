package no.nav.dokarkiv.core.exceptions;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class IllegalJournalStatusException extends FunctionalRecoverableException {

    public IllegalJournalStatusException(String message) {
        super(message);
    }
}
