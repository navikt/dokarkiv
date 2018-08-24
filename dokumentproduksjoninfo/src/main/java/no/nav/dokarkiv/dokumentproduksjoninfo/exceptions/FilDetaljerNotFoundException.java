package no.nav.dokarkiv.dokumentproduksjoninfo.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */
public class FilDetaljerNotFoundException extends DokarkivFunctionalException {

    public FilDetaljerNotFoundException(String message) {
        super(message);
    }
}
