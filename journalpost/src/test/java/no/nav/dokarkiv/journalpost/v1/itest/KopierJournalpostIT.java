package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.stream.Stream;

import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.buildJournalpost;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.A;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.OD;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.R;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.N;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class KopierJournalpostIT extends AbstractJournalpostIT {

	@ParameterizedTest
	@MethodSource
	public void shouldHappyKopierJournalpost(JournalpostTypeCode journalpostType, JournalStatusCode journalStatus) {
		restStsToken();

		JournalpostBuilder journalpostBuilder = buildJournalpost(journalpostType, journalStatus)
				.endretAvNavn("saksbehandlersen");

		Journalpost journalpost = buildAndCommit(journalpostBuilder);


		HttpEntity kopierRequestEntity = new HttpEntity<>(createHeadersWithServiceUserToken());

		ResponseEntity<String> kopierJournalpostResponse = restTemplate.exchange(URL_JOURNALPOST + KOPIER_QUERY + journalpost.getJournalpostId(), POST, kopierRequestEntity, String.class);

		assertThat(kopierJournalpostResponse.getStatusCode()).isEqualTo(CREATED);
		assertThat(kopierJournalpostResponse.getBody()).isNotEmpty();

	}

	private static Stream<Arguments> shouldHappyKopierJournalpost() {
		return Stream.of(Arguments.of(I, J),
				Arguments.of(U, FS),
				Arguments.of(U, FL),
				Arguments.of(U, E),
				Arguments.of(N, FS));
	}

	@ParameterizedTest
	@MethodSource
	public void shouldThrowBadRequestExceptionWhenJournalpostHaveInvalidStatus(JournalpostTypeCode journalpostType, JournalStatusCode journalStatus) {
		restStsToken();

		JournalpostBuilder journalpostBuilder = buildJournalpost(journalpostType, journalStatus)
				.endretAvNavn("saksbehandlersen");

		Journalpost journalpost = buildAndCommit(journalpostBuilder);


		HttpEntity kopierRequestEntity = new HttpEntity<>(createHeadersWithServiceUserToken());

		ResponseEntity<String> kopierJournalpostResponse = restTemplate.exchange(URL_JOURNALPOST + KOPIER_QUERY + journalpost.getJournalpostId(), POST, kopierRequestEntity, String.class);

		assertThat(kopierJournalpostResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
	}

	private static Stream<Arguments> shouldThrowBadRequestExceptionWhenJournalpostHaveInvalidStatus() {
		return Stream.of(Arguments.of(I, R),
				Arguments.of(I, A),
				Arguments.of(U, M),
				Arguments.of(U, A),
				Arguments.of(U, MO),
				Arguments.of(U, OD),
				Arguments.of(N, M));
	}

	@ParameterizedTest
	@ValueSource(strings = {"123"})
	public void shouldThrowNotFoundWhenJournalpostNotFoundInJoark(String journalpostId) {

		stubAzure();
		restStsToken();

		HttpEntity kopierRequestEntity = new HttpEntity<>(createHeadersWithServiceUserToken());

		ResponseEntity<String> kopierJournalpostResponse = restTemplate.exchange(URL_JOURNALPOST + KOPIER_QUERY + journalpostId, POST, kopierRequestEntity, String.class);

		assertThat(kopierJournalpostResponse.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(kopierJournalpostResponse.getBody()).isNotEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "NAV"})
	public void shouldThrowBadRequestWhenJournalpostIdAreNullOrNonNumeric(String journalpostId) {
		stubAzure();
		restStsToken();

		HttpEntity kopierRequestEntity = new HttpEntity<>(createHeadersWithServiceUserToken());

		ResponseEntity<String> kopierJournalpostResponse = restTemplate.exchange(URL_JOURNALPOST + KOPIER_QUERY + journalpostId, POST, kopierRequestEntity, String.class);

		assertThat(kopierJournalpostResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(kopierJournalpostResponse.getBody()).isNotEmpty();
	}
}
