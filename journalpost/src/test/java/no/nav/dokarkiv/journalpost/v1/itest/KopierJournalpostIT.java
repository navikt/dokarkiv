package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.KopierJournalpostResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.buildJournalpost;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.KOPIER_JOURNALPOST;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.A;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
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
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class KopierJournalpostIT extends AbstractJournalpostIT {
	private static final String BAD_REQUEST_FEILMELDING = "Kan ikke kopiere journalpost med journalpostId=%s fordi journalpost har ugyldig status=%s";
	private static final String BRUKER_ID = "srvjoarkadmin";

	@ParameterizedTest
	@MethodSource("journalpostTypeMedGyldigJournalpostStatus")
	public void shouldHappyKopierJournalpost(JournalpostTypeCode journalpostType, JournalStatusCode journalStatus, JournalStatusCode kopierJournalStatus) {
		restStsToken();

		JournalpostBuilder journalpostBuilder = buildJournalpost(journalpostType, journalStatus)
				.endretAvNavn("saksbehandlersen");

		Journalpost originalJournalpost = buildAndCommit(journalpostBuilder);


		HttpEntity<Object> kopierRequestEntity = new HttpEntity<>(createHeadersWithServiceUserToken());

		ResponseEntity<KopierJournalpostResponse> journalpostResponse = restTemplate.exchange(URL_JOURNALPOST + KOPIER_QUERY + originalJournalpost.getJournalpostId(), POST, kopierRequestEntity, KopierJournalpostResponse.class);
		KopierJournalpostResponse kopierJournalpostResponse = journalpostResponse.getBody();

		TestTransaction.start();

		Journalpost kopiertJournalpost = journalpostTestRepository.findById(Long.valueOf(kopierJournalpostResponse.getKopierJournalpostId())).orElseThrow(RuntimeException::new);

		assertThat(journalpostResponse.getStatusCode()).isEqualTo(CREATED);
		assertThat(journalpostResponse.getBody()).isNotNull();

		assertThat(kopiertJournalpost.getBehandlingstema()).isEqualTo(originalJournalpost.getBehandlingstema());
		assertThat(kopiertJournalpost.getAvsenderMottakerIdType()).isEqualTo(originalJournalpost.getAvsenderMottakerIdType());
		assertThat(kopiertJournalpost.getJournalposttype()).isEqualTo(originalJournalpost.getJournalposttype());
		assertThat(kopiertJournalpost.getFagomrade()).isEqualTo(originalJournalpost.getFagomrade());
		assertThat(kopiertJournalpost.getAvsenderMottaker()).isEqualTo(originalJournalpost.getAvsenderMottaker());
		assertThat(kopiertJournalpost.getAvsenderMottakerId()).isEqualTo(originalJournalpost.getAvsenderMottakerId());
		assertThat(kopiertJournalpost.getMottakskanal()).isEqualTo(originalJournalpost.getMottakskanal());
		assertThat(kopiertJournalpost.getInnhold()).isEqualTo(originalJournalpost.getInnhold());
		assertThat(kopiertJournalpost.getJournalstatus()).isEqualTo(kopierJournalStatus);
		assertThat(kopiertJournalpost.getKanalReferanseId()).isNotBlank();
		assertThat(kopiertJournalpost.getOpprettetKildeNavn()).isEqualTo(BRUKER_ID);
		kopiertJournalpost.getJournalpostDokumentInfoRelasjoner().forEach(jpdok -> {
			assertThat(jpdok.getOpprettetKildeNavn()).isEqualTo(BRUKER_ID);
			assertThat(jpdok.getEndretKildeNavn()).isEqualTo(BRUKER_ID);
		});
		assertThat(kopiertJournalpost.getBrukere().size()).isEqualTo(originalJournalpost.getBrukere().size());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();

		aksjonsLoggList.forEach(aksjonsLogg -> {
			assertThat(aksjonsLogg.getUtfoertAv()).isEqualTo(BRUKER_ID);
			assertThat(aksjonsLogg.getJournalpostId()).isEqualTo(originalJournalpost.getJournalpostId());
			assertThat(aksjonsLogg.getAksjon()).isEqualTo(KOPIER_JOURNALPOST);
			aksjonsLogg.getArkivElementEndringer().forEach(arkivElementEndring -> {
				assertThat(arkivElementEndring.getFraVerdi()).isEqualTo(valueOf(originalJournalpost.getJournalpostId()));
				assertThat(arkivElementEndring.getTilVerdi()).isEqualTo(valueOf(kopiertJournalpost.getJournalpostId()));
			});
		});

	}

	private static Stream<Arguments> journalpostTypeMedGyldigJournalpostStatus() {
		return Stream.of(Arguments.of(I, J, M),
				Arguments.of(U, FS, D),
				Arguments.of(U, FL, D),
				Arguments.of(U, E, D),
				Arguments.of(N, FS, D));
	}

	@ParameterizedTest
	@MethodSource("journalpostTypeMedUgyldigJournalpostStatus")
	public void shouldThrowBadRequestExceptionWhenJournalpostHaveInvalidStatus(JournalpostTypeCode journalpostType, JournalStatusCode journalStatus) {
		restStsToken();

		JournalpostBuilder journalpostBuilder = buildJournalpost(journalpostType, journalStatus)
				.endretAvNavn("saksbehandlersen");

		Journalpost journalpost = buildAndCommit(journalpostBuilder);


		HttpEntity<Object> kopierRequestEntity = new HttpEntity<>(createHeadersWithServiceUserToken());

		ResponseEntity<String> kopierJournalpostResponse = restTemplate.exchange(URL_JOURNALPOST + KOPIER_QUERY + journalpost.getJournalpostId(), POST, kopierRequestEntity, String.class);

		assertThat(kopierJournalpostResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(kopierJournalpostResponse.getBody()).contains(format(BAD_REQUEST_FEILMELDING, journalpost.getJournalpostId(), journalpost.getJournalstatus()));
	}

	private static Stream<Arguments> journalpostTypeMedUgyldigJournalpostStatus() {
		return Stream.of(Arguments.of(I, R),
				Arguments.of(I, A),
				Arguments.of(U, M),
				Arguments.of(U, A),
				Arguments.of(U, MO),
				Arguments.of(U, OD),
				Arguments.of(N, M));
	}

	@Test
	public void shouldThrowNotFoundWhenJournalpostNotFoundInJoark() {
		restStsToken();

		HttpEntity<Object> kopierRequestEntity = new HttpEntity<>(createHeadersWithServiceUserToken());

		ResponseEntity<String> kopierJournalpostResponse = restTemplate.exchange(URL_JOURNALPOST + KOPIER_QUERY + "123", POST, kopierRequestEntity, String.class);

		assertThat(kopierJournalpostResponse.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(kopierJournalpostResponse.getBody()).contains("Kunne ikke finne journalpost med journalpostId=123 i joark");
	}

	@ParameterizedTest
	@MethodSource("ugyldigJournalpostIdArguments")
	public void shouldThrowBadRequestWhenJournalpostIdAreNullOrNonNumeric(String journalpostId, String message) {
		stubAzure();
		restStsToken();

		HttpEntity<Object> kopierRequestEntity = new HttpEntity<>(createHeadersWithServiceUserToken());

		ResponseEntity<String> kopierJournalpostResponse = restTemplate.exchange(URL_JOURNALPOST + KOPIER_QUERY + journalpostId, POST, kopierRequestEntity, String.class);

		assertThat(kopierJournalpostResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(kopierJournalpostResponse.getBody()).contains(message);
	}

	private static Stream<Arguments> ugyldigJournalpostIdArguments() {
		return Stream.of(
				Arguments.of("", "kildeJournalpostId kan ikke være null eller tomt. kildeJournalpostId="),
				Arguments.of("NAV", "kildeJournalpostId må være et heltall. Mottatt verdi=NAV. kildeJournalpostId=NAV")
		);
	}
}
