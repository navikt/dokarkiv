package no.nav.dokarkiv.logiskslettdokument.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class JournalpostDokumentInfoRelasjonNotFoundException extends DokarkivFunctionalException {

	public JournalpostDokumentInfoRelasjonNotFoundException(String message) {
		super(message);
	}
}
