package no.nav.dokarkiv.core.exceptions;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class JournalpostNotFoundException extends FunctionalRecoverableException {

    public JournalpostNotFoundException(String message) {
        super(message);
    }
}
