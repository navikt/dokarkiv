package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.dokumentfil.InitierDokumentFilOpplastingRequest;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.APPLICATION_YAML_VALUE;

class InitierDokumentFilOpplastingValidatorTest {

	private static final String IDEMPOTENCY_KEY = "41ff35f0-dae9-41f8-80b0-4137592c16ca";
	private static final String CRC32C_BASE64 = Base64.getEncoder().encodeToString(new byte[]{(byte) 0x9a, 0x28, 0x54, (byte) 0xb8});

	@Test
	void shouldValidateRequest() {
		var request = new InitierDokumentFilOpplastingRequest(APPLICATION_PDF_VALUE, 482133L, CRC32C_BASE64);

		var result = InitierDokumentFilOpplastingValidator.validate(IDEMPOTENCY_KEY, request);

		assertThat(result.idempotencyKey()).isEqualTo(IDEMPOTENCY_KEY);
		assertThat(result.mediaType()).isEqualTo(APPLICATION_PDF_VALUE);
		assertThat(result.stoerrelse()).isEqualTo(482133L);
		assertThat(result.crc32c().crc32c()).containsExactly((byte) 0x9a, 0x28, 0x54, (byte) 0xb8);
	}

	@Test
	void shouldRejectNullRequest() {
		assertThatThrownBy(() -> InitierDokumentFilOpplastingValidator.validate(IDEMPOTENCY_KEY, null))
				.isInstanceOf(InputValideringFeiletException.class)
				.hasMessageContaining("Request body");
	}

	@Test
	void shouldRejectInvalidIdempotencyKey() {
		assertThatThrownBy(() -> InitierDokumentFilOpplastingValidator.validate("ugyldig key med mellomrom", validRequest()))
				.isInstanceOf(InputValideringFeiletException.class)
				.hasMessageContaining("Idempotency-Key");
	}

	@Test
	void shouldRejectUnsupportedMediaType() {
		var request = new InitierDokumentFilOpplastingRequest(APPLICATION_YAML_VALUE, 1L, CRC32C_BASE64);

		assertThatThrownBy(() -> InitierDokumentFilOpplastingValidator.validate(IDEMPOTENCY_KEY, request))
				.isInstanceOf(InputValideringFeiletException.class)
				.hasMessageContaining(APPLICATION_YAML_VALUE);
	}

	@Test
	void shouldRejectNonPositiveSize() {
		var request = new InitierDokumentFilOpplastingRequest(APPLICATION_PDF_VALUE, 0L, CRC32C_BASE64);

		assertThatThrownBy(() -> InitierDokumentFilOpplastingValidator.validate(IDEMPOTENCY_KEY, request))
				.isInstanceOf(InputValideringFeiletException.class)
				.hasMessageContaining("positivt");
	}

	@Test
	void shouldRejectInvalidBase64() {
		var request = new InitierDokumentFilOpplastingRequest(APPLICATION_PDF_VALUE, 1L, "ikke-base64");

		assertThatThrownBy(() -> InitierDokumentFilOpplastingValidator.validate(IDEMPOTENCY_KEY, request))
				.isInstanceOf(InputValideringFeiletException.class)
				.hasMessageContaining("base64");
	}

	@Test
	void shouldRejectCrc32cWithWrongLength() {
		var request = new InitierDokumentFilOpplastingRequest(APPLICATION_PDF_VALUE, 1L,
				Base64.getEncoder().encodeToString(new byte[5]));

		assertThatThrownBy(() -> InitierDokumentFilOpplastingValidator.validate(IDEMPOTENCY_KEY, request))
				.isInstanceOf(InputValideringFeiletException.class)
				.hasMessageContaining("nøyaktig 4 bytes");
	}

	private static InitierDokumentFilOpplastingRequest validRequest() {
		return new InitierDokumentFilOpplastingRequest(APPLICATION_PDF_VALUE, 1L, CRC32C_BASE64);
	}
}
