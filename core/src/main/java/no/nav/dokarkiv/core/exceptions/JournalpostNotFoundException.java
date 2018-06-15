package no.nav.dokarkiv.core.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class JournalpostNotFoundException extends FunctionalRecoverableException {

    public JournalpostNotFoundException(String message) {
        super(message);
    }
}
