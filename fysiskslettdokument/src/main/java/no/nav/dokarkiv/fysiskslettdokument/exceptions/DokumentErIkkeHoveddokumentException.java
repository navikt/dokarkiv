package no.nav.dokarkiv.fysiskslettdokument.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class DokumentErIkkeHoveddokumentException extends DokarkivFunctionalException {
	public DokumentErIkkeHoveddokumentException(String message) {
		super(message);
	}
}
