package no.nav.dokarkiv.safintern;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static no.nav.dokarkiv.safintern.journalstatus.SafinternJournalStatusService.MAX_PAGE_SIZE;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "antallRader kan ikke være større enn " + MAX_PAGE_SIZE + "!")
public class UgyldigQueryPageSizeException extends RuntimeException {

	public UgyldigQueryPageSizeException(long requestedPageSize) {
		super("antallRader kan ikke være større enn " + MAX_PAGE_SIZE + " - var " + requestedPageSize);
	}
}
