package no.nav.dokarkiv.safintern.journalstatus;

import no.nav.dokarkiv.core.exceptions.UgyldigJournalStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "FinnJournalposterStatus støtter kun søk på Journalstatus UB og U")
class UgyldigJournalstatusException extends RuntimeException {

	UgyldigJournalstatusException() {
		super("FinnJournalposterStatus støtter kun søk på Journalstatus UB og U");
	}
}
