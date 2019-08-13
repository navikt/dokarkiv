package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class KanIkkeOppdatereDistribusjonsinfoException extends DokarkivFunctionalException {
    public KanIkkeOppdatereDistribusjonsinfoException(String message) {
        super(message);
    }
}
