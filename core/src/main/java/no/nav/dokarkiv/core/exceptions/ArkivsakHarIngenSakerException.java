package no.nav.dokarkiv.core.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(code = NOT_FOUND)
public class ArkivsakHarIngenSakerException extends DokarkivFunctionalException {

	public ArkivsakHarIngenSakerException(String message) {
		super(message);
	}

}