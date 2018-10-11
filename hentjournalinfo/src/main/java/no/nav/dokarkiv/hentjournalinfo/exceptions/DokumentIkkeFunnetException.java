package no.nav.dokarkiv.hentjournalinfo.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class DokumentIkkeFunnetException extends DokarkivFunctionalException {

    public DokumentIkkeFunnetException(String message) {
        super(message);
    }
}
