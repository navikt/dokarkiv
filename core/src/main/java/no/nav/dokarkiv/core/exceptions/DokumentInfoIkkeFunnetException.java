package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND)
public final class DokumentInfoIkkeFunnetException extends DokarkivFunctionalException {
    public DokumentInfoIkkeFunnetException() {
        super();
    }

    public DokumentInfoIkkeFunnetException(String message) {
        super(message);
    }
}
