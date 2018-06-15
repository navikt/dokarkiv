package no.nav.dokarkiv.core.exceptions;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class DokumentInfoNotFoundException extends FunctionalRecoverableException {

    public DokumentInfoNotFoundException(String message) {
        super(message);
    }
}
