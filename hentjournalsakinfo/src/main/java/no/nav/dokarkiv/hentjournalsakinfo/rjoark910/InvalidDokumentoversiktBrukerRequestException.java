package no.nav.dokarkiv.hentjournalsakinfo.rjoark910;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Ugyldig rjoark910 request. Må ha aktørId eller orgnr. fraDato kan ikke være null.")
class InvalidDokumentoversiktBrukerRequestException extends DokarkivFunctionalException  {
	InvalidDokumentoversiktBrukerRequestException(String message) {
		super(message);
	}
}
