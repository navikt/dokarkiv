package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.APPLICATION_YAML_VALUE;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;

class LastOppDokumentFilValidatorTest {

	private static final String SHA256_BASE64 = Base64.getEncoder().encodeToString("a".repeat(32).getBytes());
	private static final String CONTENT_DIGEST = "sha-256=:" + SHA256_BASE64 + ":";
	private static final String IDEMPOTENCY_KEY = "41ff35f0-dae9-41f8-80b0-4137592c16ca";

	@Test
	void shouldValidatePdf() {
		Sha256ContentDigest sha256ContentDigest = LastOppDokumentFilValidator.validate(APPLICATION_PDF_VALUE, CONTENT_DIGEST, IDEMPOTENCY_KEY);
		assertThat(sha256ContentDigest.base64()).isEqualTo(SHA256_BASE64);
	}

	@Test
	void shouldValidateXml() {
		LastOppDokumentFilValidator.validate(TEXT_XML_VALUE, CONTENT_DIGEST, IDEMPOTENCY_KEY);
	}

	@Test
	void shouldValidateJson() {
		LastOppDokumentFilValidator.validate(APPLICATION_JSON_VALUE, CONTENT_DIGEST, IDEMPOTENCY_KEY);
	}

	@Test
	void shouldThrowWhenContentTypeMissing() {
		assertThatThrownBy(() -> LastOppDokumentFilValidator.validate(null, CONTENT_DIGEST, IDEMPOTENCY_KEY))
				.isInstanceOf(InputValideringFeiletException.class)
				.hasMessageContaining(CONTENT_TYPE);
	}

	@Test
	void shouldThrowWhenContentDigestMissing() {
		assertThatThrownBy(() -> LastOppDokumentFilValidator.validate(APPLICATION_PDF_VALUE, "", IDEMPOTENCY_KEY))
				.isInstanceOf(InputValideringFeiletException.class)
				.hasMessageContaining("Content-Digest");
	}

	@Test
	void shouldThrowWhenIdempotencyKeyMissing() {
		assertThatThrownBy(() -> LastOppDokumentFilValidator.validate(APPLICATION_PDF_VALUE, CONTENT_DIGEST, " "))
				.isInstanceOf(InputValideringFeiletException.class)
				.hasMessageContaining("Idempotency-Key");
	}

	@Test
	void shouldThrowWhenContentTypeNotSupported() {
		assertThatThrownBy(() -> LastOppDokumentFilValidator.validate(APPLICATION_YAML_VALUE, CONTENT_DIGEST, IDEMPOTENCY_KEY))
				.isInstanceOf(InputValideringFeiletException.class)
				.hasMessageContaining("Content-Type=" + APPLICATION_YAML_VALUE);
	}

	@Test
	void shouldThrowWhenContentDigestHasWrongAlgorithm() {
		String contentDigest = "sha-512=:" + SHA256_BASE64 + ":";
		assertThatThrownBy(() -> LastOppDokumentFilValidator.validate(APPLICATION_PDF_VALUE, contentDigest, IDEMPOTENCY_KEY))
				.isInstanceOf(InputValideringFeiletException.class)
				.hasMessageContaining("Content-Digest");
	}

	@Test
	void shouldThrowWhenContentDigestHasInvalidBase64() {
		String contentDigest = "sha-256=:not-valid-base64!!:";
		assertThatThrownBy(() -> LastOppDokumentFilValidator.validate(APPLICATION_PDF_VALUE, contentDigest, IDEMPOTENCY_KEY))
				.isInstanceOf(InputValideringFeiletException.class);
	}

	@Test
	void shouldThrowWhenContentDigestHasWrongDecodedLength() {
		String base64Of10Bytes = Base64.getEncoder().encodeToString("a".repeat(10).getBytes());
		String contentDigest = "sha-256=:" + base64Of10Bytes + ":";
		assertThatThrownBy(() -> LastOppDokumentFilValidator.validate(APPLICATION_PDF_VALUE, contentDigest, IDEMPOTENCY_KEY))
				.isInstanceOf(InputValideringFeiletException.class)
				.hasMessageContaining("ugyldig format");
	}

	@Test
	void shouldThrowWhenIdempotencyKeyTooLong() {
		String idempotencyKey = "a".repeat(513);
		assertThatThrownBy(() -> LastOppDokumentFilValidator.validate(APPLICATION_PDF_VALUE, CONTENT_DIGEST, idempotencyKey))
				.isInstanceOf(InputValideringFeiletException.class)
				.hasMessageContaining("512 tegn");
	}

	@Test
	void shouldThrowWhenIdempotencyKeyHasIllegalCharacters() {
		assertThatThrownBy(() -> LastOppDokumentFilValidator.validate(APPLICATION_PDF_VALUE, CONTENT_DIGEST, "ugyldig key med mellomrom"))
				.isInstanceOf(InputValideringFeiletException.class)
				.hasMessageContaining("Idempotency-Key");
	}
}
