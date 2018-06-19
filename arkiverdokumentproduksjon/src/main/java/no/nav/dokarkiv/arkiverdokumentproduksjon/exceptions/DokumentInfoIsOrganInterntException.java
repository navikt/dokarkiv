package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class DokumentInfoIsOrganInterntException extends FunctionalRecoverableException {

    public DokumentInfoIsOrganInterntException(String message) {
        super(message);
    }
}
