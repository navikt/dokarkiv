package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class DokumentInfoInnskrenketPartsinnsynException extends FunctionalRecoverableException {

    public DokumentInfoInnskrenketPartsinnsynException(String message) {
        super(message);
    }
}
