package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class IllegalJournalStatusException extends DokarkivFunctionalException {

    public IllegalJournalStatusException(String message) {
        super(message);
    }
}
