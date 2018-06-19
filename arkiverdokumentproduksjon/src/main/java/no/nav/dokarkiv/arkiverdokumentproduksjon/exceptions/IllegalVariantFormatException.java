package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class IllegalVariantFormatException extends FunctionalRecoverableException {

    public IllegalVariantFormatException(String message) {
        super(message);
    }
}
