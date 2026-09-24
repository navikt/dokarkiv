package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/// Kastes når det allerede finnes en opplasting for samme eksternDokumentReferanseId
/// (Idempotency-Key), men med en annet sha256Sjekksum (ulikt dokumentinnhold).
@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
public class DokumentFilUliktInnholdException extends DokarkivFunctionalException {

	public DokumentFilUliktInnholdException(String message) {
		super(message);
	}
}
