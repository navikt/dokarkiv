package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class PostLogiskVedleggRequestValidationException extends DokarkivFunctionalException{

	public PostLogiskVedleggRequestValidationException(String message) {
		super(message);
	}
}
