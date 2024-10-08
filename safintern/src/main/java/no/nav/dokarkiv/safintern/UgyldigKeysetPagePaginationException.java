package no.nav.dokarkiv.safintern;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static no.nav.dokarkiv.safintern.journalstatus.SafinternJournalStatusService.MAX_PAGE_SIZE;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Ugyldig peker for paginering")
public class UgyldigKeysetPagePaginationException extends RuntimeException {

	public UgyldigKeysetPagePaginationException(String feilmelding) {
		super("kunne ikke parse peker for paginering: " + feilmelding);
	}
}
