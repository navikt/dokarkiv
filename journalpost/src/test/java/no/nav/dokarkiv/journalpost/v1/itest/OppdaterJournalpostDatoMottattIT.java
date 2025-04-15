package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationProblemDetail;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.buildJournalpost;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.OK;

public class OppdaterJournalpostDatoMottattIT extends AbstractJournalpostIT {

	@ParameterizedTest
	@ValueSource(strings = {
			"1744182775271",
			"\"2025-04-09T09:12:55.271\"",
			"\"2025-04-09T07:12:55.271Z\"",
			"\"2025-04-09T09:12:55.271+02:00\"",
			"\"2025-04-09T07:12:55.271+00:00\"",
			"\"2025-04-09T07:12:55.271+0000\"",
			"\"2025-04-09T07:12:55.271000000Z\""
	})
	public void shouldOppdaterJournalpostWithDatoMottatt(String datoMottatt) {
		restStsToken();
		String eksternReferanseId = UUID.randomUUID().toString();
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.kanalReferanseId(eksternReferanseId)
				.endretAvNavn("saksbehandlersen")
				.mottakskanal(MottaksKanalCode.SKAN_IM));
		Long journalpostId = journalpost.getJournalpostId();

		String request = classpathToString("__files/oppdaterJournalpost_datoMottatt.json")
				.replace("{{datoMottatt}}", datoMottatt);
		HttpEntity<String> requestHttpEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(apiJournalpostPath(journalpostId.toString()), PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());

		Journalpost oppdaterJournalpost = journalpostTestRepository.findByKanalReferanseId(eksternReferanseId).orElse(new Journalpost());
		assertThat(oppdaterJournalpost.getMottattDato().toString()).isEqualTo("2025-04-09T09:12:55.271");
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"\"2025-04-09\""
	})
	public void shouldOppdaterJournalpostWithDatoMottattOnlyDate(String datoMottatt) {
		restStsToken();
		String eksternReferanseId = UUID.randomUUID().toString();
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.kanalReferanseId(eksternReferanseId)
				.endretAvNavn("saksbehandlersen")
				.mottakskanal(MottaksKanalCode.SKAN_IM));
		Long journalpostId = journalpost.getJournalpostId();

		String request = classpathToString("__files/oppdaterJournalpost_datoMottatt.json")
				.replace("{{datoMottatt}}", datoMottatt);
		HttpEntity<String> requestHttpEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(apiJournalpostPath(journalpostId.toString()), PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());

		Journalpost oppdaterJournalpost = journalpostTestRepository.findByKanalReferanseId(eksternReferanseId).orElse(new Journalpost());
		assertThat(oppdaterJournalpost.getMottattDato().toString()).isEqualTo("2025-04-09T00:00");
	}

	@Test
	void shouldReturnProblemDetailWhenJsonMappingError() {
		restStsToken();
		String eksternReferanseId = UUID.randomUUID().toString();
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.kanalReferanseId(eksternReferanseId)
				.endretAvNavn("saksbehandlersen")
				.mottakskanal(MottaksKanalCode.SKAN_IM));
		Long journalpostId = journalpost.getJournalpostId();

		String request = classpathToString("__files/oppdaterJournalpost_datoMottatt.json")
				.replace("{{datoMottatt}}", "\"01.01.2025\"");
		HttpEntity<String> requestHttpEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());

		ResponseEntity<ApplicationProblemDetail> responseEntity = restTemplate.exchange(apiJournalpostPath(journalpostId.toString()), PUT, requestHttpEntity, ApplicationProblemDetail.class);

		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody().getMessage()).contains("Klarte ikke parse tekst=01.01.2025 til LocalDateTime");
	}
}