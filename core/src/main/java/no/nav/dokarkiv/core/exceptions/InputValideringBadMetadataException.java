package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class InputValideringBadMetadataException extends DokarkivFunctionalException {

    public InputValideringBadMetadataException(String message) {
        super(message);
    }
}
