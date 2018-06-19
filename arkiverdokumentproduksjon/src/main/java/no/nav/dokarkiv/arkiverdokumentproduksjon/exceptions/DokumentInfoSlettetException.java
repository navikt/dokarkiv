package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class DokumentInfoSlettetException extends FunctionalRecoverableException {

    public DokumentInfoSlettetException(String message) {
        super(message);
    }
}
