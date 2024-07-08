package no.nav.dokarkiv.safintern.journalstatus;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "fraDato er obligatorisk i FinnJournalposterStatus")
class UgyldigJournalstatusQueryStartDatoException extends RuntimeException {

	UgyldigJournalstatusQueryStartDatoException() {
		super("fraDato er obligatorisk i FinnJournalposterStatus");
	}
}
