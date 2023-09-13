package no.nav.dokarkiv.exception;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(NOT_FOUND)
public class ArkivVariantkkeFunnetException extends DokarkivFunctionalException {

	public ArkivVariantkkeFunnetException(String message) {
		super(message);
	}
}
