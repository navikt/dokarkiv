package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class IllegalJournalStatusException extends FunctionalRecoverableException {

    public IllegalJournalStatusException(String message) {
        super(message);
    }
}
