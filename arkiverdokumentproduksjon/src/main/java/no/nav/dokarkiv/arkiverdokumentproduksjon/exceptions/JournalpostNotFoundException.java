package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class JournalpostNotFoundException extends DokarkivFunctionalException {

    public JournalpostNotFoundException(String message) {
        super(message);
    }
}
