package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
public class InitierDokumentFilAnnenTjenesteException extends DokarkivFunctionalException {
	public InitierDokumentFilAnnenTjenesteException(String message) {
		super(message);
	}
}
