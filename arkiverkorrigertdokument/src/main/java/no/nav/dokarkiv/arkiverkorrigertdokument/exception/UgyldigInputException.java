package no.nav.dokarkiv.arkiverkorrigertdokument.exception;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public final class UgyldigInputException extends DokarkivFunctionalException {

	public UgyldigInputException(String message) {
		super(message);
	}

}
