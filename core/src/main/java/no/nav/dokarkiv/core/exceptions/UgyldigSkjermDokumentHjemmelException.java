package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UgyldigSkjermDokumentHjemmelException extends DokarkivFunctionalException {
	public UgyldigSkjermDokumentHjemmelException(String message) {
		super(message);
	}
}
