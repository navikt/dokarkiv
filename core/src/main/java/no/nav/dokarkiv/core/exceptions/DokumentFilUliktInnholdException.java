package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
public class DokumentFilUliktInnholdException extends DokarkivFunctionalException {

	public DokumentFilUliktInnholdException(String message) {
		super(message);
	}
}
