package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
public class UgyldigMediaTypeException extends DokarkivFunctionalException {

	public UgyldigMediaTypeException(String message) {
		super(message);
	}

}
