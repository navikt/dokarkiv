package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/// Kastes når en eller flere `dokumentFilId` allerede er brukt av en annen journalpost
/// (tilstand ARKIVERES eller ARKIVERT). Klienten skal ikke forsøke på nytt.
@ResponseStatus(code = HttpStatus.UNPROCESSABLE_CONTENT)
public class DokumentFilAlleredeBruktException extends DokarkivFunctionalException {

	public DokumentFilAlleredeBruktException(String message) {
		super(message);
	}
}
