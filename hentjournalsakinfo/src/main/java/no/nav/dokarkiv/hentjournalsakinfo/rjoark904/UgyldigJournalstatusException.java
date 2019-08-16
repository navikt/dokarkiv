package no.nav.dokarkiv.hentjournalsakinfo.rjoark904;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "FinnJournalposterStatus støtter kun søk på Journalstatus UB og U")
class UgyldigJournalstatusException extends RuntimeException {
}
