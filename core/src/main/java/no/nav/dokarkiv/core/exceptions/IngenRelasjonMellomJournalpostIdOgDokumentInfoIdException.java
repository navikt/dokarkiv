package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException extends DokarkivFunctionalException {

	public IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException(String message) {
		super(message);
	}
}