package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class IllegalVariantFormatException extends DokarkivFunctionalException {

    public IllegalVariantFormatException(String message) {
        super(message);
    }
}
