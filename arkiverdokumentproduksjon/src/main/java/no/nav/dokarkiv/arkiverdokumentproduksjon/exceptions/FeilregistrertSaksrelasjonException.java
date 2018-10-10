package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class FeilregistrertSaksrelasjonException extends DokarkivFunctionalException {

    public FeilregistrertSaksrelasjonException(String message) {
        super(message);
    }
}
