package no.nav.dokarkiv.arkiverkorrigertdokument.exception;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public final class VariantFormatCodeNotFoundException extends DokarkivFunctionalException {
	public VariantFormatCodeNotFoundException(String message) {
		super(message);
	}
}
