package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class DokumentInfoInnskrenketPartsinnsynException extends DokarkivFunctionalException {

    public DokumentInfoInnskrenketPartsinnsynException(String message) {
        super(message);
    }
}
