package no.nav.dokarkiv.safintern;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "fraDato er obligatorisk")
public class UgyldigJournalpostQueryStartDatoException extends RuntimeException {

	public UgyldigJournalpostQueryStartDatoException() {
		super("fraDato er obligatorisk");
	}
}
