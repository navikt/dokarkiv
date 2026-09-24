package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.entities.DokumentFilOpplasting;
import no.nav.dokarkiv.core.storage.InMemoryBucketStorage;
import no.nav.dokarkiv.core.util.Digest;
import no.nav.dokarkiv.journalpost.v1.api.dokumentfil.DokumentFilResponse;
import no.nav.dokarkiv.journalpost.v1.api.dokumentfil.DokumentFilTilstand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Base64;
import java.util.UUID;

import static no.nav.dokarkiv.core.domain.codes.DokumentFilOpplastingTilstand.LASTET_OPP;
import static no.nav.dokarkiv.core.util.Digest.sha256;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

class LastOppDokumentFilIT extends AbstractJournalpostIT {

	public static final byte[] DOKUMENT_FIL_PAYLOAD = "Dokumentinnhold".getBytes();

	@Autowired
	private InMemoryBucketStorage dokarkivMellomlagerBucketStorage;

	@AfterEach
	void resetDokumentFilOpplastingDb() {
		dokumentFilOpplastingTestRepository.deleteAll();
		commitAndStartNewTransaction();
		dokarkivMellomlagerBucketStorage.clear();
	}

	@Test
	void shouldReturnCreatedWhenValidRequest() {
		byte[] fil = DOKUMENT_FIL_PAYLOAD;
		String idempotencyKey = UUID.randomUUID().toString();
		byte[] sha256 = sha256(fil);
		var headers = lagHeadere(idempotencyKey, sha256);
		var requestEntity = new HttpEntity<>(fil, headers);

		var result = restTemplate.exchange(apiDokumentFilPath(), POST, requestEntity, DokumentFilResponse.class);

		assertThat(result.getStatusCode()).isEqualTo(CREATED);
		DokumentFilResponse dokumentFilResponse = result.getBody();
		UUID dokumentFilId = dokumentFilResponse.getDokumentFilId();
		assertThat(dokumentFilId).isNotNull();
		assertThat(dokumentFilResponse.getEksternDokumentReferanseId()).isEqualTo(idempotencyKey);
		assertThat(dokumentFilResponse.getMediaType()).isEqualTo(APPLICATION_PDF_VALUE);
		assertThat(dokumentFilResponse.getSha256Sjekksum()).containsExactly(sha256);
		assertThat(dokumentFilResponse.getTilstand()).isEqualTo(DokumentFilTilstand.LASTET_OPP);
		assertThat(dokumentFilResponse.getDatoOpprettet()).isNotNull();
		assertThat(dokumentFilResponse.getDatoSistEndret()).isNotNull();

		commitAndStartNewTransaction();
		DokumentFilOpplasting dokumentFilOpplasting = dokumentFilOpplastingTestRepository.findById(dokumentFilId).orElseThrow();
		assertThat(dokumentFilOpplasting.getEksternDokumentReferanseId()).isEqualTo(idempotencyKey);
		assertThat(dokumentFilOpplasting.getMediaType()).isEqualTo(APPLICATION_PDF_VALUE);
		assertThat(dokumentFilOpplasting.getSha256Sjekksum()).containsExactly(sha256);
		assertThat(dokumentFilOpplasting.getTilstand()).isEqualTo(LASTET_OPP);
		assertThat(dokumentFilOpplasting.getAntallBytes()).isEqualTo(fil.length);
		assertThat(dokumentFilOpplasting.getDatoLease()).isNotNull();
		assertThat(dokumentFilOpplasting.getDatoOpprettet()).isNotNull();
		assertThat(dokumentFilOpplasting.getOpprettetKildeNavn()).isEqualTo("dokarkiv-itest");
		assertThat(dokumentFilOpplasting.getEndretKildeNavn()).isEqualTo("dokarkiv-itest");
		assertThat(dokarkivMellomlagerBucketStorage.getObject(dokumentFilOpplasting.getDokumentFilId().toString())).isEqualTo(fil);
	}

	@Test
	void shouldReturnBadRequestWhenContentDigestIsMissing() {
		byte[] fil = DOKUMENT_FIL_PAYLOAD;
		var headers = lagHeadere(UUID.randomUUID().toString(), sha256(fil));
		headers.remove("Content-Digest");
		var requestEntity = new HttpEntity<>(fil, headers);

		var result = restTemplate.exchange(apiDokumentFilPath(), POST, requestEntity, String.class);

		assertThat(result.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(result.getBody()).contains("Required header 'Content-Digest' is not present.");
	}

	@Test
	void shouldReturnBadRequestWhenContentTypeIsNotSupported() {
		byte[] fil = DOKUMENT_FIL_PAYLOAD;
		var headers = lagHeadere(UUID.randomUUID().toString(), sha256(fil));
		headers.setContentType(MediaType.APPLICATION_YAML);
		var requestEntity = new HttpEntity<>(fil, headers);

		var result = restTemplate.exchange(apiDokumentFilPath(), POST, requestEntity, String.class);

		assertThat(result.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(result.getBody()).contains("Header Content-Type=application/yaml er ikke støttet.");
	}

	@Test
	void shouldReturnBadRequestWhenIdempotencyKeyIsMissing() {
		byte[] fil = DOKUMENT_FIL_PAYLOAD;
		var headers = lagHeadere(UUID.randomUUID().toString(), sha256(fil));
		headers.remove("Idempotency-Key");
		var requestEntity = new HttpEntity<>(fil, headers);

		var result = restTemplate.exchange(apiDokumentFilPath(), POST, requestEntity, String.class);

		assertThat(result.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(result.getBody()).contains("Required header 'Idempotency-Key' is not present.");
	}

	@Test
	void shouldReturnBadRequestWhenSha256DoesNotMatchPayload() {
		byte[] fil = DOKUMENT_FIL_PAYLOAD;
		byte[] annenFil = "Et helt annet innhold".getBytes();
		var headers = lagHeadere("01a0ec79-f596-76e9-8e73-706f18ecde4d", sha256(annenFil));
		var requestEntity = new HttpEntity<>(fil, headers);

		var result = restTemplate.exchange(apiDokumentFilPath(), POST, requestEntity, String.class);

		assertThat(result.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(result.getBody()).contains("Beregnet sha256 sjekksum av mottatt payload stemmer ikke med Content-Digest for Idempotency-Key=01a0ec79-f596-76e9-8e73-706f18ecde4d. " +
				"Beregnet=bJfpLzsndupFnI5aebT64uqZIOYfr4N9Ubj4QrMSpwE=, " +
				"Content-Digest=+dPSLH8jNb8QVVwwTdcxyNn41RTF+GEHnq0wEzZCIcc=");
	}

	@Test
	void shouldReturnCreatedForIdempotentRetryOfSameFile() {
		byte[] fil = DOKUMENT_FIL_PAYLOAD;
		String idempotencyKey = UUID.randomUUID().toString();
		var headers = lagHeadere(idempotencyKey, sha256(fil));

		var result1 = restTemplate.exchange(apiDokumentFilPath(), POST, new HttpEntity<>(fil, headers), DokumentFilResponse.class);
		assertThat(result1.getStatusCode()).isEqualTo(CREATED);
		DokumentFilResponse dokumentFilResponse1 = result1.getBody();
		assertThat(dokumentFilResponse1.getEksternDokumentReferanseId()).isEqualTo(idempotencyKey);

		var result2 = restTemplate.exchange(apiDokumentFilPath(), POST, new HttpEntity<>(fil, headers), DokumentFilResponse.class);
		assertThat(result2.getStatusCode()).isEqualTo(CREATED);
		DokumentFilResponse dokumentFilResponse2 = result2.getBody();
		assertThat(dokumentFilResponse2.getEksternDokumentReferanseId()).isEqualTo(idempotencyKey);
	}

	@Test
	void shouldReturnConflictWhenIdempotencyKeyIsUnderOpplasting() {
		byte[] fil = DOKUMENT_FIL_PAYLOAD;
		String idempotencyKey = "7d5002a9-e6cf-4453-acf0-05be1c178360";
		var naa = java.time.LocalDateTime.now();
		var dokumentFilOpplasting = new DokumentFilOpplasting(UUID.randomUUID(), idempotencyKey, APPLICATION_PDF_VALUE, sha256(fil), "itest", naa.plusMinutes(2));
		dokumentFilOpplastingTestRepository.persist(dokumentFilOpplasting);
		commitAndStartNewTransaction();

		var headers = lagHeadere(idempotencyKey, sha256(fil));
		var result = restTemplate.exchange(apiDokumentFilPath(), POST, new HttpEntity<>(fil, headers), String.class);

		assertThat(result.getStatusCode()).isEqualTo(CONFLICT);
		assertThat(result.getBody()).contains("Idempotency-Key=7d5002a9-e6cf-4453-acf0-05be1c178360 brukes allerede av en opplasting som pågår");
	}

	@Test
	void shouldReturnUnprocessableWhenIdempotencyKeyHasDifferentContent() {
		byte[] fil = DOKUMENT_FIL_PAYLOAD;
		byte[] annenFil = "Et helt annet innhold enn forrige gang".getBytes();
		String idempotencyKey = "c999c42d-2125-4256-873a-ab205029584c";
		var naa = java.time.LocalDateTime.now();
		var dokumentFilOpplasting = new DokumentFilOpplasting(UUID.randomUUID(), idempotencyKey, APPLICATION_PDF_VALUE, sha256(annenFil), "itest", naa.plusMinutes(2));
		dokumentFilOpplastingTestRepository.persist(dokumentFilOpplasting);
		commitAndStartNewTransaction();

		var headers = lagHeadere(idempotencyKey, sha256(fil));
		var result = restTemplate.exchange(apiDokumentFilPath(), POST, new HttpEntity<>(fil, headers), String.class);

		assertThat(result.getStatusCode()).isEqualTo(UNPROCESSABLE_CONTENT);
		assertThat(result.getBody()).contains("Idempotency-Key=c999c42d-2125-4256-873a-ab205029584c er allerede brukt for opplasting av et dokument med et annet innhold sha256=mihUuA==");
	}

	private HttpHeaders lagHeadere(String idempotencyKey, byte[] sha256) {
		HttpHeaders headers = createHeadersWithClientCredentialToken();
		headers.setContentType(APPLICATION_PDF);
		headers.set("Content-Digest", "sha-256=:" + Base64.getEncoder().encodeToString(sha256) + ":");
		headers.set("Idempotency-Key", idempotencyKey);
		return headers;
	}
}
