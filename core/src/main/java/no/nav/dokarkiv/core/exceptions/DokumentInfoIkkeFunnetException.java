package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public final class DokumentInfoIkkeFunnetException extends DokarkivFunctionalException {
    public DokumentInfoIkkeFunnetException(String message) {
        super(message);
    }

    public DokumentInfoIkkeFunnetException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
