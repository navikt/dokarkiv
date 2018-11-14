package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class DokumentIkkeLogiskSlettetException extends DokarkivFunctionalException {

	public DokumentIkkeLogiskSlettetException(String message) {
		super(message);
	}
}
