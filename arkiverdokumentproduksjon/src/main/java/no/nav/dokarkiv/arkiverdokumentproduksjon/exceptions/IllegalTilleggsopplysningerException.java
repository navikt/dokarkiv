package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class IllegalTilleggsopplysningerException extends FunctionalRecoverableException {

    public IllegalTilleggsopplysningerException(String message) {
        super(message);
    }
}
