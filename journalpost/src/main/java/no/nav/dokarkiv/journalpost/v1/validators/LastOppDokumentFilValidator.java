package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;

import java.util.Set;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.domain.validator.EksternReferanseIdValidator.EKSTERN_REFERANSE_ID_PATTERN;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;

public final class LastOppDokumentFilValidator {

	public static final int IDEMPOTENCY_KEY_MAX_LENGTH = 512;
	public static final Set<String> TILLATTE_CONTENT_TYPES = Set.of(APPLICATION_PDF_VALUE,
			APPLICATION_XML_VALUE,
			TEXT_XML_VALUE,
			APPLICATION_JSON_VALUE);

	private LastOppDokumentFilValidator() {
		// no-op
	}

	/// Validerer headerne til LastOppDokumentfil-tjenesten.
	///
	/// @param contentType    verdien til Content-Type-headeren.
	/// @param contentDigest  verdien til Content-Digest-headeren.
	/// @param idempotencyKey verdien til Idempotency-Key-headeren.
	/// @return et validert [ContentDigest].
	public static ContentDigest validate(String contentType, String contentDigest, String idempotencyKey) {
		validateHeaderErSatt(contentType, "Content-Type");
		validateHeaderErSatt(contentDigest, "Content-Digest");
		validateHeaderErSatt(idempotencyKey, "Idempotency-Key");

		validateContentType(contentType);
		validateIdempotencyKey(idempotencyKey);

		return ContentDigest.parse(contentDigest);
	}

	private static void validateHeaderErSatt(String verdi, String headerNavn) {
		if (isBlank(verdi)) {
			throw new InputValideringFeiletException(format("Header %s må være satt", headerNavn));
		}
	}

	private static void validateContentType(String contentType) {
		if (!TILLATTE_CONTENT_TYPES.contains(contentType)) {
			throw new InputValideringFeiletException(format(
					"Header Content-Type=%s er ikke støttet. Må være en av %s", contentType, TILLATTE_CONTENT_TYPES));
		}
	}

	private static void validateIdempotencyKey(String idempotencyKey) {
		if (idempotencyKey.length() > IDEMPOTENCY_KEY_MAX_LENGTH) {
			throw new InputValideringFeiletException(format(
					"Header Idempotency-Key kan ikke være over %d tegn. Mottatt lengde=%d", IDEMPOTENCY_KEY_MAX_LENGTH, idempotencyKey.length()));
		}
		if (!EKSTERN_REFERANSE_ID_PATTERN.matcher(idempotencyKey).matches()) {
			throw new InputValideringFeiletException(format(
					"Header Idempotency-Key=%s kan bare inneholde alfanumeriske tegn og følgende spesialtegn :;,.=-_~$&+*\"\\@!", idempotencyKey));
		}
	}
}
