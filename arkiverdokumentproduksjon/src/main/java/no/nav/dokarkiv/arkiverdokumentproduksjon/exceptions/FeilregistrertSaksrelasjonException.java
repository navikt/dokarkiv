package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class FeilregistrertSaksrelasjonException extends FunctionalRecoverableException {

    public FeilregistrertSaksrelasjonException(String message) {
        super(message);
    }
}
