package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class DokumentInfoIsOrganInterntException extends DokarkivFunctionalException {

    public DokumentInfoIsOrganInterntException(String message) {
        super(message);
    }
}
