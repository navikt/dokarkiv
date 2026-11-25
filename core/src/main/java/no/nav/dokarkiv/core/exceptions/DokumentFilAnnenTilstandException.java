package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
public class DokumentFilAnnenTilstandException extends DokarkivFunctionalException {

	public DokumentFilAnnenTilstandException(String message) {
		super(message);
	}
}
