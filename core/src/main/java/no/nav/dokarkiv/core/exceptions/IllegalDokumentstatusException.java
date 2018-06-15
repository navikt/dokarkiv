package no.nav.dokarkiv.core.exceptions;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class IllegalDokumentstatusException extends FunctionalRecoverableException {

    public IllegalDokumentstatusException(String message) {
        super(message);
    }
}
