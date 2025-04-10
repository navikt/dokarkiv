package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import static no.nav.dokarkiv.core.CoreConfig.ZONEID_NORGE;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.buildJournalpost;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.OK;

public class OppdaterJournalpostDatoMottattIT extends AbstractJournalpostIT {

	@ParameterizedTest
	@ValueSource(strings = {
			"1744182775271"
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
		LocalDateTime localDateTime = oppdaterJournalpost.getMottattDato().toInstant()
				.atZone(ZONEID_NORGE).toLocalDateTime();
		assertThat(localDateTime.toString()).isEqualTo("2025-04-09T09:12:55.271");
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"\"2025-04-09\"",
			// Det er kun dato-presisjon på oppdaterJournalpost pga @JsonFormat, derfor mister disse tiden
			"\"2025-04-09T07:12:55.271\"",
			"\"2025-04-09T07:12:55.271Z\"",
			"\"2025-04-09T09:12:55.271+02:00\"",
			"\"2025-04-09T07:12:55.271+00:00\"",
			"\"2025-04-09T07:12:55.271000000Z\""
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
		LocalDateTime localDateTime = oppdaterJournalpost.getMottattDato().toInstant()
				.atZone(ZONEID_NORGE).toLocalDateTime();
		assertThat(localDateTime.toString()).isEqualTo("2025-04-09T02:00");
	}
}