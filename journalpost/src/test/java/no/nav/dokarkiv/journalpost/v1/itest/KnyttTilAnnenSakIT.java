package no.nav.dokarkiv.journalpost.v1.itest;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.dokarkiv.journalpost.v1.api.KnyttTilAnnenSakRequest;
import no.nav.dokarkiv.journalpost.v1.api.KnyttTilAnnenSakResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createKnyttTilAnnenSakRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

//@RunWith(SpringRunner.class)
public class KnyttTilAnnenSakIT extends AbstractJournalpostIT{

	public static final String KOPIERT_JOURNALPOST_ID = "333333333";
	private static final String OIDC_TOKEN = "eyAidHlwIjogIkpXVCIsICJraWQiOiAiMWwySmtDb1RMMTBibWVBeHlsZzR4Umk4ajJZPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICJ4RklSS0dpTWZ4ZFVPS3c0ZmQ4MW9BIiwgInN1YiI6ICJaOTkyMzEwIiwgImF1ZGl0VHJhY2tpbmdJZCI6ICJiZDdlYWE0ZC1mYzIzLTQ2ZGMtOGRjZi1iMjJmNzU1NDExZjQtMjAyMDc5MzQiLCAiaXNzIjogImh0dHBzOi8vaXNzby1xLmFkZW8ubm86NDQzL2lzc28vb2F1dGgyIiwgInRva2VuTmFtZSI6ICJpZF90b2tlbiIsICJhdWQiOiAiaWRhLXEiLCAiY19oYXNoIjogInctbGx3ZlJMenVpRFBselpkY1BhenciLCAib3JnLmZvcmdlcm9jay5vcGVuaWRjb25uZWN0Lm9wcyI6ICIyZmNlNWU1ZS02ODdjLTQ5ZmYtOTRjYS1jNzE2OGVmY2M2MmQiLCAiYXpwIjogImlkYS1xIiwgImF1dGhfdGltZSI6IDE1NTUwNzQ3NjcsICJyZWFsbSI6ICIvIiwgImV4cCI6IDE1NTUwNzgzNjcsICJ0b2tlblR5cGUiOiAiSldUVG9rZW4iLCAiaWF0IjogMTU1NTA3NDc2NyB9.orrUotLp8SMkCpigVhkAUlw9Rx5tigBrYNVv3j8fTmkIe-I1MEI0xctxM-tnLbrgcW3I-3Ye_bkS4KplhR4spnG9hT45L1dD-yoLsu8R6cD1PklMsx8m93XmaTHDReGZAI3uKO4KSPcQHyVE7-tIc6CWYqbVXWmEUxUsHNYm3bWO_0rZ-Su6CWVCEBz3yWa85rUcPn0Il-_BWkgF-0YhOWJn3ndKAl_96ARmR-nllhUnQDYqHk2DwYLWnz_WOb4HuuqxKRP5i1h8zHwGIR6VORCzWgFViiFNTPT54Mtr2fZtVinP8W70JoRZ1pKbk-bYK4ErJgACU8npdGBZYTZa6g";
	private static final String SERVICEUSER_TOKEN = "eyAidHlwIjogIkpXVCIsICJraWQiOiAiMWwySmtDb1RMMTBibWVBeHlsZzR4Umk4ajJZPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICJ4RklSS0dpTWZ4ZFVPS3c0ZmQ4MW9BIiwgInN1YiI6ICJaOTkyMzEwIiwgImF1ZGl0VHJhY2tpbmdJZCI6ICJiZDdlYWE0ZC1mYzIzLTQ2ZGMtOGRjZi1iMjJmNzU1NDExZjQtMjAyMDc5MzQiLCAiaXNzIjogImh0dHBzOi8vaXNzby1xLmFkZW8ubm86NDQzL2lzc28vb2F1dGgyIiwgInRva2VuTmFtZSI6ICJpZF90b2tlbiIsICJhdWQiOiAiaWRhLXEiLCAiY19oYXNoIjogInctbGx3ZlJMenVpRFBselpkY1BhenciLCAib3JnLmZvcmdlcm9jay5vcGVuaWRjb25uZWN0Lm9wcyI6ICIyZmNlNWU1ZS02ODdjLTQ5ZmYtOTRjYS1jNzE2OGVmY2M2MmQiLCAiYXpwIjogImlkYS1xIiwgImF1dGhfdGltZSI6IDE1NTUwNzQ3NjcsICJyZWFsbSI6ICIvIiwgImV4cCI6IDE1NTUwNzgzNjcsICJ0b2tlblR5cGUiOiAiSldUVG9rZW4iLCAiaWF0IjogMTU1NTA3NDc2NyB9.orrUotLp8SMkCpigVhkAUlw9Rx5tigBrYNVv3j8fTmkIe-I1MEI0xctxM-tnLbrgcW3I-3Ye_bkS4KplhR4spnG9hT45L1dD-yoLsu8R6cD1PklMsx8m93XmaTHDReGZAI3uKO4KSPcQHyVE7-tIc6CWYqbVXWmEUxUsHNYm3bWO_0rZ-Su6CWVCEBz3yWa85rUcPn0Il-_BWkgF-0YhOWJn3ndKAl_96ARmR-nllhUnQDYqHk2DwYLWnz_WOb4HuuqxKRP5i1h8zHwGIR6VORCzWgFViiFNTPT54Mtr2fZtVinP8W70JoRZ1pKbk-bYK4ErJgACU8npdGBZYTZa6g";
	public static final String URL_JOURNALPOST = "/rest/journalpostapi/v1/journalpost/";
	public static final String KNYTT_TIL_ANNEN_SAK = "/knyttTilAnnenSak";
	public static final String NAV_CALL_ID = "Nav-CallId";
	private static final String NAV_CONSUMER_ID = "Nav-Consumer-Id";
	private static final String GYLDIG_FNR = "01018912345";
	public static final String UGYLDIG_FNR = "0101891234";
	public static final String JOURNALFOERENDE_ENHET = "9999";  // Ved automatisk journalføring uten mennesker involvert, skal enhet settes til "9999".
	public static final String FAGSAK = "FAGSAK";
	public static final String FAGSAK_ID = "0123A21";
	public static final String FAGSAKSYSTEM = "IT01";
	public static final String TEMA = "SYK";

	@BeforeEach
	public void tearDown() {
		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();
	}

	@Test
	public void knyttTilAnnenSakHappyPath() {
		abacPermit();
		restStsToken();
		happyAktoerIdStub();
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponseKildeJournalpostId1-happy.json")));

		Long journalpostId = joarkRepository.save(createJournalpostWithHoveddokument()).getJournalpostId();
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		HttpEntity<KnyttTilAnnenSakRequest> requestEntity = new HttpEntity<>(createKnyttTilAnnenSakRequestHappyPath(), createHeadersWithUserAndServiceUserTokenAndConsumerId());
		KnyttTilAnnenSakResponse response = callKnyttTilAnnenSakRequestAndAssertResponseCode(requestEntity, HttpStatus.OK, journalpostId);

		assertNotNull(response);

		verify(exactly(1), postRequestedFor(urlEqualTo("/safgraphql"))
				.withRequestBody(equalToJson(String.format("""
						{
						  "query": "query journalpost($queryJournalpostId: String!) {\\n  journalpost(journalpostId: $queryJournalpostId) {\\n    dokumenter {\\n      dokumentInfoId\\n      dokumentvarianter {\\n        saksbehandlerHarTilgang\\n        variantformat\\n      }\\n    }\\n  }\\n}\\n",
						  "operationName": "journalpost",
						  "variables": {
						    "queryJournalpostId": "%s"
						  }
						}""", journalpostId)))
				.withHeader("X-Correlation-ID", matching(NAV_CALL_ID)));

	}

	private KnyttTilAnnenSakResponse callKnyttTilAnnenSakRequestAndAssertResponseCode(HttpEntity requestEntity, HttpStatus expectedStatus, long journalpostId) {
		ResponseEntity<KnyttTilAnnenSakResponse> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + KNYTT_TIL_ANNEN_SAK, HttpMethod.PUT, requestEntity, KnyttTilAnnenSakResponse.class);
		assertEquals(expectedStatus, response.getStatusCode());
		return response.getBody();
	}

	public static KnyttTilAnnenSakRequest createKnyttTilAnnenSakRequestHappyPath() {
		return createKnyttTilAnnenSakRequest(FAGSAK, FAGSAK_ID, FAGSAKSYSTEM, TEMA, FNR, GYLDIG_FNR, JOURNALFOERENDE_ENHET);
	}

	public static HttpHeaders createHeadersWithConsumerToken(String consumerToken){
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + OIDC_TOKEN);
		headers.add("Nav-CallId", NAV_CALL_ID);
		headers.add("Nav-Consumer-Id", NAV_CONSUMER_ID);
		headers.add("Nav-Consumer-Token", consumerToken);
		return headers;
	}
}
