package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class KanIkkeLeggeTilVedleggException extends DokarkivFunctionalException {

	public KanIkkeLeggeTilVedleggException(String message) {
		super(message);
	}
}
