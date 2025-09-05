package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.endrejournalstatus.EndreJournalstatusRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.List;
import java.util.stream.Stream;

import static no.nav.dokarkiv.core.util.TestdataFactory.createJournalpost;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class EndreJournalstatusIT extends AbstractJournalpostIT {

	@Value("${azure.ad.admin.role}")
	String joarkVedlikeholdGruppeId;

	@ParameterizedTest
	@MethodSource
	void successfullyEndreJournalstatusForJournalpost(String statusEndresTil, JournalStatusCode journalStatusResult, AksjonsTypeCode aksjonsTypeCode) {
		Journalpost journalpost = createJournalpost(null, JournalpostTypeCode.I, JournalStatusCode.M);
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		EndreJournalstatusRequest request = new EndreJournalstatusRequest(statusEndresTil);

		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken(AZP_NAME_JOARKADMIN, MS_USER_ID_WITHOUT_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		String url = apiJournalpostPath(journalpostId + ENDRE_JOURNALSTATUS);
		ResponseEntity<Void> response = restTemplate.exchange(url, POST, requestEntity, Void.class);

		assertEquals(NO_CONTENT, response.getStatusCode());

		TestTransaction.start();
		Journalpost endretJournalpost = journalpostTestRepository.findById(journalpost.getJournalpostId()).orElseThrow(RuntimeException::new);

		assertEquals(NAV_USER_NAME, endretJournalpost.getEndretAvNavn());
		assertThat(AZP_NAME_JOARKADMIN).containsIgnoringCase(endretJournalpost.getEndretKildeNavn());
		assertEquals(NAV_USER_ID, endretJournalpost.getChangeStamp().getUpdatedBy());
		assertEquals(journalStatusResult, endretJournalpost.getJournalstatus());
		assertThat(endretJournalpost.getChangeStamp().getUpdatedDate()).isAfter(journalpost.getChangeStamp().getCreatedDate());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertEquals(1, aksjonsLoggList.size());
		assertEquals(NAV_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(aksjonsTypeCode, aksjonsLoggList.get(0).getAksjon());
		assertEquals(1, aksjonsLoggList.get(0).getArkivElementEndringer().size());

		TestTransaction.end();
	}

	static Stream<Arguments> successfullyEndreJournalstatusForJournalpost() {
		return Stream.of(
				Arguments.of("MOTTATT", JournalStatusCode.MO, AksjonsTypeCode.TILBAKE_TIL_MOTTATT),
				Arguments.of("UKJENT_BRUKER", JournalStatusCode.UB, AksjonsTypeCode.UKJENT_BRUKER),
				Arguments.of("UTGAAR", JournalStatusCode.U, AksjonsTypeCode.UTGAAR)
		);
	}

	@Test
	void shouldReceiveForbiddenStatusIfJoarkVedlikeholdClaimMissingInToken() {
		Journalpost journalpost = createJournalpost(null, JournalpostTypeCode.I, JournalStatusCode.M);
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		EndreJournalstatusRequest request = new EndreJournalstatusRequest("UKJENT_BRUKER");

		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken(AZP_NAME_JOARKADMIN, MS_USER_ID_WITHOUT_GROUP_ACCESS));
		String url = apiJournalpostPath(journalpostId + ENDRE_JOURNALSTATUS);
		ResponseEntity<Void> response = restTemplate.exchange(url, POST, requestEntity, Void.class);

		assertEquals(FORBIDDEN, response.getStatusCode());
	}

	@Test
	void shouldReceiveUnauthorizedIfNotEntraIdOboToken() {
		Journalpost journalpost = createJournalpost(null, JournalpostTypeCode.I, JournalStatusCode.M);
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		EndreJournalstatusRequest request = new EndreJournalstatusRequest("UKJENT_BRUKER");

		var requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		String url = apiJournalpostPath(journalpostId + ENDRE_JOURNALSTATUS);
		ResponseEntity<Void> response = restTemplate.exchange(url, POST, requestEntity, Void.class);

		assertEquals(UNAUTHORIZED, response.getStatusCode());
	}
	@ParameterizedTest
	@EnumSource(value = JournalStatusCode.class, names = {"M", "MO", "U", "UB"}, mode = EnumSource.Mode.INCLUDE)
	void successfullyEndreJournalstatusForJournalpost(JournalStatusCode journalStatusCode) {
		Journalpost journalpost = createJournalpost(null, JournalpostTypeCode.I, journalStatusCode);
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		EndreJournalstatusRequest request = new EndreJournalstatusRequest("UKJENT_BRUKER");

		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken(AZP_NAME_JOARKADMIN, MS_USER_ID_WITHOUT_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		String url = apiJournalpostPath(journalpostId + ENDRE_JOURNALSTATUS);
		ResponseEntity<Void> response = restTemplate.exchange(url, POST, requestEntity, Void.class);

		assertEquals(NO_CONTENT, response.getStatusCode());
	}

	@ParameterizedTest
	@EnumSource(value = JournalStatusCode.class, names = {"M", "MO", "U", "UB"}, mode = EnumSource.Mode.EXCLUDE)
	void denyEndreJournalstatusForJournalpostWithInvalidStatus(JournalStatusCode journalStatusCode) {
		Journalpost journalpost = createJournalpost(null, JournalpostTypeCode.I, journalStatusCode);
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		EndreJournalstatusRequest request = new EndreJournalstatusRequest("UKJENT_BRUKER");

		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken(AZP_NAME_JOARKADMIN, MS_USER_ID_WITHOUT_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		String url = apiJournalpostPath(journalpostId + ENDRE_JOURNALSTATUS);
		ResponseEntity<String> response = restTemplate.exchange(url, POST, requestEntity, String.class);

		assertEquals(BAD_REQUEST, response.getStatusCode());
		assertThat(response.getBody()).contains("Journalpost har ikke en av gyldige statuser [M, U, MO, UB]");
	}

	@ParameterizedTest
	@EnumSource(value = JournalpostTypeCode.class, names = {"U", "N"})
	void denyEndreJournalstatusForJournalpostWithInvalidType(JournalpostTypeCode journalpostTypeCode) {
		Journalpost journalpost = createJournalpost(null, journalpostTypeCode, JournalStatusCode.M);
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		EndreJournalstatusRequest request = new EndreJournalstatusRequest("UKJENT_BRUKER");

		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken(AZP_NAME_JOARKADMIN, MS_USER_ID_WITHOUT_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		String url = apiJournalpostPath(journalpostId + ENDRE_JOURNALSTATUS);
		ResponseEntity<String> response = restTemplate.exchange(url, POST, requestEntity, String.class);

		assertEquals(BAD_REQUEST, response.getStatusCode());
		assertThat(response.getBody()).contains("Journalpost er ikke av type Inngående");
	}

	@Test
	void denyEndreJournalstatusForJournalpostWithInvalidEndresTil() {
		Journalpost journalpost = createJournalpost(null, JournalpostTypeCode.I, JournalStatusCode.M);
		journalpostTestRepository.persist(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long journalpostId = journalpost.getJournalpostId();
		EndreJournalstatusRequest request = new EndreJournalstatusRequest("BRUKJENT_UKER");

		var requestEntity = new HttpEntity<>(request, createHeadersWithOboToken(AZP_NAME_JOARKADMIN, MS_USER_ID_WITHOUT_GROUP_ACCESS, joarkVedlikeholdGruppeId));
		String url = apiJournalpostPath(journalpostId + ENDRE_JOURNALSTATUS);
		ResponseEntity<String> response = restTemplate.exchange(url, POST, requestEntity, String.class);

		assertEquals(BAD_REQUEST, response.getStatusCode());
		assertThat(response.getBody()).containsIgnoringCase("Ugyldig verdi for Journalstatus: BRUKJENT_UKER");
	}
}
