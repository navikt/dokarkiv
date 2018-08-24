package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class IllegalDokumentstatusException extends DokarkivFunctionalException {

    public IllegalDokumentstatusException(String message) {
        super(message);
    }
}
