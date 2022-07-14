package no.nav.dokarkiv.journalpost.v1.itest;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.KnyttTilAnnenSakRequest;
import no.nav.dokarkiv.journalpost.v1.api.KnyttTilAnnenSakResponse;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.List;

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
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.ENDRE_METADATA;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.KOPIER_JOURNALPOST;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.SAKSTILKNYTNING;
import static no.nav.dokarkiv.core.util.TestDataGenerator.AVSENDER_MOTTAKER_NAVN;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.util.TestDataUtils.AVSENDER_MOTTAKER_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.JOURNALPOST_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createKnyttTilAnnenSakRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class KnyttTilAnnenSakIT extends AbstractJournalpostIT{

	public static final String URL_JOURNALPOST = "/rest/journalpostapi/v1/journalpost/";
	public static final String KNYTT_TIL_ANNEN_SAK = "/knyttTilAnnenSak";
	public static final String NAV_CALL_ID = "Nav-CallId";
	private static final String GYLDIG_FNR = "01018912345";
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
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponseKildeJournalpostId1-happy.json")));

		Long journalpostId = joarkRepository.save(createJournalpostWithHoveddokument()).getJournalpostId();
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		HttpEntity<KnyttTilAnnenSakRequest> requestEntity = new HttpEntity<>(createKnyttTilAnnenSakRequestHappyPath(), createHeadersWithUserAndServiceUserTokenAndConsumerId());
		ResponseEntity<KnyttTilAnnenSakResponse> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + KNYTT_TIL_ANNEN_SAK, HttpMethod.PUT, requestEntity, KnyttTilAnnenSakResponse.class);
		assertEquals(OK, response.getStatusCode());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Journalpost journalpost = joarkRepository.findById(response.getBody().getNyJournalpostId()).orElseThrow(RuntimeException::new);

		assertNotNull(response);

		assertEquals(AvsenderMottakerIdTypeCode.FNR, journalpost.getAvsenderMottakerIdType());
		assertEquals(AVSENDER_MOTTAKER_NAVN, journalpost.getAvsenderMottaker());
		assertEquals(AVSENDER_MOTTAKER_ID, journalpost.getAvsenderMottakerId());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertEquals(4, aksjonsLoggList.size());
		valdiateAksjonsloggELement(aksjonsLoggList.get(0), KOPIER_JOURNALPOST, journalpostId, "srvdokarkivproxy");
		valdiateAksjonsloggELement(aksjonsLoggList.get(1), ENDRE_METADATA, journalpost.getJournalpostId(), "Z990782");
		valdiateAksjonsloggELement(aksjonsLoggList.get(2), SAKSTILKNYTNING, journalpost.getJournalpostId(), "Z990782");
		valdiateAksjonsloggELement(aksjonsLoggList.get(3), AksjonsTypeCode.FERDIGSTILL, journalpost.getJournalpostId(), "consumer_id");

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

	@Test
	public void knyttTilAnnenSakJournalpostNotFound() {
		abacPermit();
		restStsToken();
		happyAktoerIdStub();

		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponseJournalpostIkkeFunnet.json")));

		HttpEntity<KnyttTilAnnenSakRequest> requestEntity = new HttpEntity<>(createKnyttTilAnnenSakRequestHappyPath(), createHeadersWithUserAndServiceUserTokenAndConsumerId());
		ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + JOURNALPOST_ID + KNYTT_TIL_ANNEN_SAK, HttpMethod.PUT, requestEntity, String.class);
		assertEquals(NOT_FOUND, response.getStatusCode());

		verify(exactly(1), postRequestedFor(urlEqualTo("/safgraphql")));
	}

	private void valdiateAksjonsloggELement(AksjonsLogg aksjonsloggElement, AksjonsTypeCode aksjonsType, long journalpostId, String utfoertAv){
		assertEquals(utfoertAv, aksjonsloggElement.getUtfoertAv());
		assertEquals(aksjonsType, aksjonsloggElement.getAksjon());
		assertEquals(journalpostId, aksjonsloggElement.getJournalpostId());
	}

	public static KnyttTilAnnenSakRequest createKnyttTilAnnenSakRequestHappyPath() {
		return createKnyttTilAnnenSakRequest(FAGSAK, FAGSAK_ID, FAGSAKSYSTEM, TEMA, FNR, GYLDIG_FNR, JOURNALFOERENDE_ENHET);
	}

}
