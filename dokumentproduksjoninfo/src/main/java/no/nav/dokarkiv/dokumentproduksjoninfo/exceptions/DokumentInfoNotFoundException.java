package no.nav.dokarkiv.dokumentproduksjoninfo.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class DokumentInfoNotFoundException extends FunctionalRecoverableException {

    public DokumentInfoNotFoundException(String message) {
        super(message);
    }
}
