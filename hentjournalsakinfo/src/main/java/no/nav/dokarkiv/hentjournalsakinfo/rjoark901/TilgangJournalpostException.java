package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TilgangJournalpostException extends DokarkivFunctionalException {

    public TilgangJournalpostException(String message, Throwable cause) {
        super(message, cause);
    }

    public TilgangJournalpostException(String message) {
        super(message);
    }

}

