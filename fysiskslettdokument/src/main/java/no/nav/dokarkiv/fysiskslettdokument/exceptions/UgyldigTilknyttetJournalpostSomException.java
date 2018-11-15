package no.nav.dokarkiv.fysiskslettdokument.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class UgyldigTilknyttetJournalpostSomException extends DokarkivFunctionalException {
	public UgyldigTilknyttetJournalpostSomException(String message) {
		super(message);
	}
}
