package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class IllegalTilleggsopplysningerException extends DokarkivFunctionalException {

    public IllegalTilleggsopplysningerException(String message) {
        super(message);
    }
}
