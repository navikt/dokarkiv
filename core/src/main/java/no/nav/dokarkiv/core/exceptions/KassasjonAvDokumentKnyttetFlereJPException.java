package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_IMPLEMENTED)
public class KassasjonAvDokumentKnyttetFlereJPException extends DokarkivFunctionalException {

	public KassasjonAvDokumentKnyttetFlereJPException(String message) {
		super(message);
	}
}
