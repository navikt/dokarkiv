package no.nav.dokarkiv.core.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */
public class FilDetaljerNotFoundException extends FunctionalRecoverableException {

    public FilDetaljerNotFoundException(String message) {
        super(message);
    }
}
