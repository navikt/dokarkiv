package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class DokumentInfoSlettetException extends DokarkivFunctionalException {

    public DokumentInfoSlettetException(String message) {
        super(message);
    }
}
