package no.nav.dokarkiv.arkivervariant.exception;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public final class VariantFormatAlreadyExistsException extends DokarkivFunctionalException {
	public VariantFormatAlreadyExistsException(String message) {
		super(message);
	}
}
