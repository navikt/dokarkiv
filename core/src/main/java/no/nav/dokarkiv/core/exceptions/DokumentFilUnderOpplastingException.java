package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/// Kastes når det allerede finnes en opplasting under arbeid (tilstand LASTES_OPP) for samme
/// eksternDokumentReferanseId (Idempotency-Key) og samme sha256Sjekksum.
@ResponseStatus(HttpStatus.CONFLICT)
public class DokumentFilUnderOpplastingException extends DokarkivFunctionalException {

	public DokumentFilUnderOpplastingException(String message) {
		super(message);
	}
}
