package no.nav.dokarkiv.core.exceptions.saf;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class SafJournalpostQueryFunctionalException extends DokarkivFunctionalException {

	public SafJournalpostQueryFunctionalException(String message) {
		super(message);
	}


}
