package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class FilDetaljerOnDemandException extends FunctionalRecoverableException {

    public FilDetaljerOnDemandException(String message) {
        super(message);
    }
}
