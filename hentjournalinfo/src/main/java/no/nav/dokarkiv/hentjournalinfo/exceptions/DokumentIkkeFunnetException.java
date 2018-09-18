package no.nav.dokarkiv.hentjournalinfo.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class DokumentIkkeFunnetException extends DokarkivFunctionalException {

    public DokumentIkkeFunnetException(String message) {
        super(message);
    }
}
