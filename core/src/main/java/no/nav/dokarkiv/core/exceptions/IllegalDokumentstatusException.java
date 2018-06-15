package no.nav.dokarkiv.core.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class IllegalDokumentstatusException extends FunctionalRecoverableException {

    public IllegalDokumentstatusException(String message) {
        super(message);
    }
}
