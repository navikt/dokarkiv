package no.nav.dokarkiv.hentjournalinfo.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class JournalpostIkkeFunnetException extends DokarkivFunctionalException {

    public JournalpostIkkeFunnetException(String message) {
        super(message);
    }
}
