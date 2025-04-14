package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.POST;

public class OpprettJournalpostDatoMottattIT extends AbstractJournalpostIT {

	@ParameterizedTest
	@ValueSource(strings = {
			"1744182775271",
			"\"2025-04-09T07:12:55.271\"",
			"\"2025-04-09T07:12:55.271Z\"",
			"\"2025-04-09T09:12:55.271+02:00\"",
			"\"2025-04-09T07:12:55.271+00:00\"",
			"\"2025-04-09T07:12:55.271000000Z\""
	})
	public void shouldOpprettJournalpostWithDatoMottatt(String datoMottatt) {
		restStsToken();

		String eksternReferanseId = UUID.randomUUID().toString();
		String request = classpathToString("__files/opprettJournalpost_datoMottatt.json")
				.replace("{{eksternReferanseId}}", eksternReferanseId)
				.replace("{{datoMottatt}}", datoMottatt);
		HttpEntity<String> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = journalpostTestRepository.findByKanalReferanseId(eksternReferanseId).orElse(new Journalpost());
		assertThat(journalpost.getMottattDato().toString()).isEqualTo("2025-04-09T09:12:55.271");
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"\"2025-04-09\""
	})
	public void shouldOpprettJournalpostWithDatoMottattOnlyDate(String datoMottatt) {
		restStsToken();

		String eksternReferanseId = UUID.randomUUID().toString();
		String request = classpathToString("__files/opprettJournalpost_datoMottatt.json")
				.replace("{{eksternReferanseId}}", eksternReferanseId)
				.replace("{{datoMottatt}}", datoMottatt);
		HttpEntity<String> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = journalpostTestRepository.findByKanalReferanseId(eksternReferanseId).orElse(new Journalpost());
		assertThat(journalpost.getMottattDato().toString()).isEqualTo("2025-04-09T02:00");
	}
}