package no.nav.dokarkiv.hentjournalinfo.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class JournalpostIkkeFunnetException extends DokarkivFunctionalException {

    public JournalpostIkkeFunnetException(String message) {
        super(message);
    }
}
