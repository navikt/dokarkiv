package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.journalpost.v1.api.AvsluttAlleSakerPaaTemaRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;
import java.util.stream.Stream;

import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.FAR;
import static no.nav.dokarkiv.journalpost.v1.controllers.DokVaktmesterController.INTERN_ROLE;
import static org.assertj.core.api.Assertions.assertThat;

public class AvsluttAlleSakerPaaTemaIT extends AbstractJournalpostIT {

	private static final String AVSLUTT_ALLE_SAKER_PAA_TEMA_PATH = "/rest/internal/journalpostapi/v1/avsluttAlleSakerPaaTema";
	private static final String TEMA = FAR.name();
	private static final String REFERANSE = "MMA-9876";

	@Autowired
	WebTestClient webTestClient;

	@Test
	void skalReturnereNoContent() {
		var request = new AvsluttAlleSakerPaaTemaRequest(TEMA, REFERANSE, null, null);

		webTestClient.patch()
				.uri(AVSLUTT_ALLE_SAKER_PAA_TEMA_PATH)
				.bodyValue(request)
				.headers(headers -> headers.setBearerAuth(lagGyldigTokenMedClaims()))
				.exchange()
				.expectStatus().isNoContent();
	}

	@ParameterizedTest
	@MethodSource
	void skalReturnereBadRequestHvisTemaEllerReferanseMangler(String tema, String referanse, String feilmelding) {
		var request = new AvsluttAlleSakerPaaTemaRequest(tema, referanse, null, null);

		var response = webTestClient.patch()
				.uri(AVSLUTT_ALLE_SAKER_PAA_TEMA_PATH)
				.bodyValue(request)
				.headers(headers -> headers.setBearerAuth(lagGyldigTokenMedClaims()))
				.exchange()
				.expectStatus().isBadRequest()
				.returnResult(String.class)
				.getResponseBody()
				.blockFirst();

		assertThat(response).containsSequence(feilmelding);
	}

	public static Stream<Arguments> skalReturnereBadRequestHvisTemaEllerReferanseMangler() {
		return Stream.of(
				Arguments.of(null, REFERANSE, "tema kan ikke være null eller tom"),
				Arguments.of(TEMA, null, "referanse kan ikke være null eller tom")
		);
	}

	@Test
	void skalReturnereUnauthorizedVedManglendeToken() {
		var request = new AvsluttAlleSakerPaaTemaRequest(TEMA, REFERANSE, null, null);

		var response = webTestClient.patch()
				.uri(AVSLUTT_ALLE_SAKER_PAA_TEMA_PATH)
				.bodyValue(request)
				.exchange()
				.expectStatus().isUnauthorized()
				.returnResult(String.class)
				.getResponseBody()
				.blockFirst();

		assertThat(response).containsSequence("Authorization headeren mangler Bearer JWT. Undersøk om Authorization header har 'Bearer ' etterfulgt av en utstedt JWT.");
	}

	@Test
	void skalReturnereUnauthorizedVedManglendeTokenClaims() {
		var request = new AvsluttAlleSakerPaaTemaRequest(TEMA, REFERANSE, null, null);

		var tokenUtenClaims = azureTokenForClientCredentialFlow(APP_CLAIM_SUB,
				Map.of(DEFAULT_CLAIM_SUB, APP_CLAIM_SUB, DEFAULT_CLAIM_OID, APP_CLAIM_SUB));

		webTestClient.patch()
				.uri(AVSLUTT_ALLE_SAKER_PAA_TEMA_PATH)
				.bodyValue(request)
				.headers(headers -> headers.setBearerAuth(tokenUtenClaims))
				.exchange()
				.expectStatus().isUnauthorized()
				.returnResult(String.class)
				.getResponseBody()
				.blockFirst();
	}

	private String lagGyldigTokenMedClaims() {
		return azureTokenForClientCredentialFlow(APP_CLAIM_SUB, Map.of(
				ROLES, INTERN_ROLE,
				DEFAULT_CLAIM_SUB, APP_CLAIM_SUB,
				DEFAULT_CLAIM_OID, APP_CLAIM_SUB)
		);
	}

}