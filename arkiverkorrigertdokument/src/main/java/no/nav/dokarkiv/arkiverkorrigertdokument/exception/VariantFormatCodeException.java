package no.nav.dokarkiv.arkiverkorrigertdokument.exception;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class VariantFormatCodeException extends DokarkivFunctionalException {
	public VariantFormatCodeException(String message) {
		super(message);
	}
}
