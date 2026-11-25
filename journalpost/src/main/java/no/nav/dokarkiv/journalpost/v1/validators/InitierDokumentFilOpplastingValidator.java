package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.dokumentfil.InitierDokumentFilOpplastingRequest;

import java.util.Base64;

import static java.lang.String.format;
import static no.nav.dokarkiv.journalpost.v1.validators.LastOppDokumentFilValidator.validateContentType;
import static no.nav.dokarkiv.journalpost.v1.validators.LastOppDokumentFilValidator.validateHeaderErSatt;
import static no.nav.dokarkiv.journalpost.v1.validators.LastOppDokumentFilValidator.validateIdempotencyKey;
import static org.apache.commons.lang3.StringUtils.isBlank;

public final class InitierDokumentFilOpplastingValidator {

	private static final int CRC32C_BYTE_LENGTH = 4;

	private InitierDokumentFilOpplastingValidator() {
		// no-op
	}

	public static ValidertInitierDokumentFilOpplastingRequest validate(
			String idempotencyKey,
			InitierDokumentFilOpplastingRequest request) {
		validateHeaderErSatt(idempotencyKey, "Idempotency-Key");
		validateIdempotencyKey(idempotencyKey);
		if (request == null) {
			throw new InputValideringFeiletException("Request body kan ikke være null");
		}

		validateMediaType(request.mediaType());
		validateStoerrelse(request.stoerrelse());
		Crc32cContentDigest crc32c = validateCrc32c(request.crc32c());

		return new ValidertInitierDokumentFilOpplastingRequest(idempotencyKey, request.mediaType(), request.stoerrelse(), crc32c);
	}

	private static void validateMediaType(String mediaType) {
		validateHeaderErSatt(mediaType, "mediaType");
		validateContentType(mediaType);
	}

	private static void validateStoerrelse(Long stoerrelse) {
		if (stoerrelse == null || stoerrelse <= 0) {
			throw new InputValideringFeiletException(format("stoerrelse må være et positivt heltall. Mottatt stoerrelse=%s", stoerrelse));
		}
	}

	private static Crc32cContentDigest validateCrc32c(String crc32c) {
		if (isBlank(crc32c)) {
			throw new InputValideringFeiletException("crc32c kan ikke være null eller tom");
		}

		byte[] decoded;
		try {
			decoded = Base64.getDecoder().decode(crc32c);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException("crc32c er ikke gyldig base64", e);
		}
		if (decoded.length != CRC32C_BYTE_LENGTH) {
			throw new InputValideringFeiletException(format(
					"crc32c må dekodes til nøyaktig %d bytes. Mottok %d bytes", CRC32C_BYTE_LENGTH, decoded.length));
		}
		return new Crc32cContentDigest(decoded);
	}

	public record ValidertInitierDokumentFilOpplastingRequest(
			String idempotencyKey,
			String mediaType,
			long stoerrelse,
			Crc32cContentDigest crc32c
	) {
	}
}
