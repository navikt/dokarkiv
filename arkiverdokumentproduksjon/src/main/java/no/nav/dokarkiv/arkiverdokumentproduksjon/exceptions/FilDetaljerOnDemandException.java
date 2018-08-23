package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class FilDetaljerOnDemandException extends DokarkivFunctionalException {

    public FilDetaljerOnDemandException(String message) {
        super(message);
    }
}
