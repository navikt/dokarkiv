package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.entities.DokumentFilOpplasting;
import no.nav.dokarkiv.core.exceptions.ApplicationProblemDetail;
import no.nav.dokarkiv.core.util.Crc32c;
import no.nav.dokarkiv.journalpost.v1.api.dokumentfil.InitierDokumentFilOpplastingRequest;
import no.nav.dokarkiv.journalpost.v1.api.dokumentfil.InitierDokumentFilOpplastingResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

class InitierDokumentFilOpplastingIT extends AbstractJournalpostIT {

	public static final String OPPLASTING = "/opplasting";
	public static final byte[] CRC32C_DIGEST = {(byte) 0x9f, 0x4d, (byte) 0xf1, (byte) 0xe8};

	@AfterEach
	void resetDatabase() {
		dokumentFilOpplastingTestRepository.deleteAll();
		commitAndStartNewTransaction();
	}

	@Test
	void shouldInitiereDokumentFilOpplasting() {
		var request = new InitierDokumentFilOpplastingRequest(
				APPLICATION_PDF_VALUE,
				482133L,
				Base64.getEncoder().encodeToString(CRC32C_DIGEST));

		var response = restTemplate.exchange(apiDokumentFilPath(OPPLASTING), POST, new HttpEntity<>(request, headers("41ff35f0-dae9-41f8-80b0-4137592c16ca")), InitierDokumentFilOpplastingResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		InitierDokumentFilOpplastingResponse body = response.getBody();
		assertThat(body.dokumentFilId().version()).isEqualTo(7);
		assertThat(body.crc32c()).isEqualTo(request.crc32c());
		assertThat(body.stoerrelse()).isEqualTo(482133L);
		assertThat(body.tilstand()).hasToString("UNDER_OPPLASTING");
		assertThat(body.datoOpprettet()).isCloseTo(LocalDateTime.now(), within(5, SECONDS));
		assertThat(body.datoSistEndret()).isCloseTo(LocalDateTime.now(), within(5, SECONDS));
		assertThat(body.opplasting().url().toString()).contains(body.dokumentFilId().toString());
		assertThat(body.opplasting().utloeper()).isAfter(body.datoOpprettet().plusMinutes(59));

		var lagret = dokumentFilOpplastingTestRepository.findById(body.dokumentFilId()).orElseThrow();
		assertThat(lagret.getAntallBytes()).isEqualTo(request.stoerrelse());
		assertThat(lagret.getOpplastingUrl()).isEqualTo(body.opplasting().url().toString());
	}

	@Test
	void shouldReturnSameUrlForIdempotentRetry() {
		String idempotencyKey = "41ff35f0-dae9-41f8-80b0-4137592c16ca";
		var request = new InitierDokumentFilOpplastingRequest(
				APPLICATION_PDF_VALUE,
				10L,
				Base64.getEncoder().encodeToString(CRC32C_DIGEST));

		var first = restTemplate.exchange(apiDokumentFilPath(OPPLASTING), POST, new HttpEntity<>(request, headers(idempotencyKey)), InitierDokumentFilOpplastingResponse.class);
		var retry = restTemplate.exchange(apiDokumentFilPath(OPPLASTING), POST, new HttpEntity<>(request, headers(idempotencyKey)), InitierDokumentFilOpplastingResponse.class);

		assertThat(retry.getStatusCode()).isEqualTo(CREATED);
		assertThat(retry.getBody().dokumentFilId()).isEqualTo(first.getBody().dokumentFilId());
		assertThat(retry.getBody().opplasting()).isEqualTo(first.getBody().opplasting());
	}

	@Test
	void shouldReturnUnprocessableForDifferentCrc32c() {
		String idempotencyKey = "c999c42d-2125-4256-873a-ab205029584c";
		var first = new InitierDokumentFilOpplastingRequest(APPLICATION_PDF_VALUE, 10L,
				Base64.getEncoder().encodeToString(new byte[]{1, 2, 3, 4}));
		var second = new InitierDokumentFilOpplastingRequest(APPLICATION_PDF_VALUE, 10L,
				Base64.getEncoder().encodeToString(new byte[]{5, 6, 7, 8}));

		restTemplate.exchange(apiDokumentFilPath(OPPLASTING), POST, new HttpEntity<>(first, headers(idempotencyKey)), InitierDokumentFilOpplastingResponse.class);
		var response = restTemplate.exchange(apiDokumentFilPath(OPPLASTING), POST, new HttpEntity<>(second, headers(idempotencyKey)), ApplicationProblemDetail.class);

		assertThat(response.getStatusCode()).isEqualTo(UNPROCESSABLE_CONTENT);
		assertThat(response.getBody().getMessage()).contains("er allerede brukt for opplasting av et dokument med et annet innhold ");
	}

	@Test
	void shouldReturnUnprocessableContentWhenTilstandNotUnderOpplasting() {
		String idempotencyKey = "7d5002a9-e6cf-4453-acf0-05be1c178360";
		var naa = java.time.LocalDateTime.now();
		var dokumentFilOpplasting = new DokumentFilOpplasting(UUID.randomUUID(), idempotencyKey, APPLICATION_PDF_VALUE, CRC32C_DIGEST, "itest", naa.plusMinutes(2));
		dokumentFilOpplasting.markerLastetOpp(10L, "test".getBytes(), LocalDateTime.now(), "itest");
		dokumentFilOpplastingTestRepository.persist(dokumentFilOpplasting);
		commitAndStartNewTransaction();

		var request = new InitierDokumentFilOpplastingRequest(
				APPLICATION_PDF_VALUE,
				482133L,
				Base64.getEncoder().encodeToString(CRC32C_DIGEST));
		var response = restTemplate.exchange(apiDokumentFilPath(OPPLASTING), POST, new HttpEntity<>(request, headers(idempotencyKey)), ApplicationProblemDetail.class);

		assertThat(response.getStatusCode()).isEqualTo(UNPROCESSABLE_CONTENT);
		assertThat(response.getBody().getMessage()).contains("er i en annen tilstand og kan ikke initialiseres lenger. tilstand=LASTET_OPP");
	}

	@Test
	void shouldReturnUnprocessableContentWhenNoOpplastingUrl() {
		String idempotencyKey = "7d5002a9-e6cf-4453-acf0-05be1c178360";
		var naa = java.time.LocalDateTime.now();
		var dokumentFilOpplasting = new DokumentFilOpplasting(UUID.randomUUID(), idempotencyKey, APPLICATION_PDF_VALUE, CRC32C_DIGEST, "itest", naa.plusMinutes(2));
		dokumentFilOpplastingTestRepository.persist(dokumentFilOpplasting);
		commitAndStartNewTransaction();

		var request = new InitierDokumentFilOpplastingRequest(
				APPLICATION_PDF_VALUE,
				482133L,
				Base64.getEncoder().encodeToString(CRC32C_DIGEST));
		var response = restTemplate.exchange(apiDokumentFilPath(OPPLASTING), POST, new HttpEntity<>(request, headers(idempotencyKey)), ApplicationProblemDetail.class);

		assertThat(response.getStatusCode()).isEqualTo(UNPROCESSABLE_CONTENT);
		assertThat(response.getBody().getMessage()).contains("er opprettet av en annen tjeneste og kan ikke initialiseres");
	}

	@Test
	void shouldReturnBadRequestWhenIdempotencyKeyIsInvalid() {
		var request = new InitierDokumentFilOpplastingRequest(
				APPLICATION_PDF_VALUE,
				1L,
				Base64.getEncoder().encodeToString(new byte[4]));

		var response = restTemplate.exchange(apiDokumentFilPath(OPPLASTING), POST, new HttpEntity<>(request, headers("ugyldig nøkkel")), String.class);

		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(response.getBody()).contains("Idempotency-Key");
	}

	@Test
	void shouldReturnBadRequestWhenCrc32cHasWrongLength() {
		var request = new InitierDokumentFilOpplastingRequest(
				APPLICATION_PDF_VALUE,
				1L,
				Base64.getEncoder().encodeToString(new byte[5]));

		var response = restTemplate.exchange(apiDokumentFilPath(OPPLASTING), POST, new HttpEntity<>(request, headers("41ff35f0-dae9-41f8-80b0-4137592c16ca")), String.class);

		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(response.getBody()).contains("nøyaktig 4 bytes");
	}

	private HttpHeaders headers(String idempotencyKey) {
		HttpHeaders headers = createHeadersWithClientCredentialToken();
		headers.setContentType(APPLICATION_JSON);
		headers.set("Idempotency-Key", idempotencyKey);
		return headers;
	}
}
