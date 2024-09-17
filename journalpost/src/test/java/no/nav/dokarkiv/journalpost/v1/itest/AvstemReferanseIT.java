package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.journalpost.v1.api.AvstemmingReferanser;
import no.nav.dokarkiv.journalpost.v1.api.FeilendeAvstemmingReferanser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.List;
import java.util.stream.Stream;

import static no.nav.dokarkiv.core.util.TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpMethod.POST;

public class AvstemReferanseIT extends AbstractJournalpostIT {

	@Test
	void shouldReturnNoContentWhenAllReferencesMatch() {
		var journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(null);
		journalpostTestRepository.persist(journalpost);
		var journalpost2 = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(null);
		journalpost2.setKanalReferanseId(journalpost2.getKanalReferanseId() + "2");
		journalpostTestRepository.persist(journalpost2);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<FeilendeAvstemmingReferanser> response = doRequestWithReferanser(journalpost.getKanalReferanseId(), journalpost2.getKanalReferanseId());

		assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT));
	}

	@Test
	void shouldReturnOkWhenSomeReferencesNotMatched() {
		var journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(null);
		journalpostTestRepository.persist(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		String eksternReferanseIkkeFunnet = "ikke_eksisterende_id";
		ResponseEntity<FeilendeAvstemmingReferanser> response = doRequestWithReferanser(journalpost.getKanalReferanseId(), eksternReferanseIkkeFunnet);

		assertThat(response.getStatusCode(), is(HttpStatus.OK));
		assertThat(response.getBody().referanserIkkeFunnet(), hasSize(1));
		assertThat(response.getBody().referanserIkkeFunnet(), contains(eksternReferanseIkkeFunnet));
	}

	@Test
	void shouldBeRejectedIfNotAuthorizedWithRoleInternSkanning() {
		var journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(null);
		journalpostTestRepository.persist(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		HttpHeaders headers = createHeadersWithServiceUserTokenAndRolesClaim("skanmothelse", "some_other_role");
		HttpEntity<AvstemmingReferanser> requestHttpEntity = new HttpEntity<>(new AvstemmingReferanser(List.of(journalpost.getKanalReferanseId())), headers);
		ResponseEntity<FeilendeAvstemmingReferanser> response = restTemplate.exchange(
				URL_JOURNALPOSTAPI + "/avstemReferanser", POST, requestHttpEntity, FeilendeAvstemmingReferanser.class);

		assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED));

	}

	@ParameterizedTest
	@MethodSource
	void shouldReturnBadRequestOnBadInput(List<String> input) {
		ResponseEntity<FeilendeAvstemmingReferanser> response = doRequestWithReferanser(input);

		assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
	}

	static Stream<Arguments> shouldReturnBadRequestOnBadInput() {
		return Stream.of(
				null,
				List.of(),
				List.of(""),
				List.of("-- drop table users; --"),
				List.of("aaa___MER_ENN_200_TEGN_____aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
		).map(Arguments::of);
	}


	private ResponseEntity<FeilendeAvstemmingReferanser> doRequestWithReferanser(String... referanser) {
		return doRequestWithReferanser(List.of(referanser));
	}

	private ResponseEntity<FeilendeAvstemmingReferanser> doRequestWithReferanser(List<String> referanser) {
		HttpHeaders headers = createHeadersWithServiceUserTokenAndRolesClaim("skanmothelse", "api_intern_skanning");
		HttpEntity<AvstemmingReferanser> requestHttpEntity = new HttpEntity<>(new AvstemmingReferanser(referanser), headers);
		return restTemplate.exchange(
				URL_JOURNALPOSTAPI + "/avstemReferanser", POST, requestHttpEntity, FeilendeAvstemmingReferanser.class);
	}
}
