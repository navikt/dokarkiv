package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class IllegalFagomraadeException extends FunctionalRecoverableException {

    public IllegalFagomraadeException(String message) {
        super(message);
    }
}
