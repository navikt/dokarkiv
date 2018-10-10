package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class DokumentInfoNotFoundException extends DokarkivFunctionalException {

    public DokumentInfoNotFoundException(String message) {
        super(message);
    }
}
