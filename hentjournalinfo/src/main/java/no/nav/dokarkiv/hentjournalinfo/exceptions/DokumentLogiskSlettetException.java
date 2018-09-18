package no.nav.dokarkiv.hentjournalinfo.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class DokumentLogiskSlettetException extends DokarkivFunctionalException {
    public DokumentLogiskSlettetException(String message) {
        super(message);
    }
}
