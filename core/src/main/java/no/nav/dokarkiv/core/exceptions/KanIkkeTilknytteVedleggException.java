package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class KanIkkeTilknytteVedleggException extends DokarkivFunctionalException {

	public KanIkkeTilknytteVedleggException(String message) {
		super(message);
	}
}
