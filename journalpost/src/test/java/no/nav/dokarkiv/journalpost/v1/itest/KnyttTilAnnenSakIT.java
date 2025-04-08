package no.nav.dokarkiv.journalpost.v1.itest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.journalpost.v1.api.knytttilannensak.KnyttTilAnnenSakRequest;
import no.nav.dokarkiv.journalpost.v1.api.knytttilannensak.KnyttTilAnnenSakResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.util.TestDataGenerator.AVSENDER_MOTTAKER_NAVN;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfoVedleggRelasjon;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.util.TestDataUtils.AVSENDER_MOTTAKER_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.JOURNALPOST_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createKnyttTilAnnenSakRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class KnyttTilAnnenSakIT extends AbstractJournalpostIT {

	public static final String KNYTT_TIL_ANNEN_SAK = "/knyttTilAnnenSak";
	public static final String NAV_CALL_ID = "itest";
	private static final String GYLDIG_FNR = "01018912345";
	public static final String JOURNALFOERENDE_ENHET = "9999";  // Ved automatisk journalføring uten mennesker involvert, skal enhet settes til "9999".
	public static final String FAGSAK = "FAGSAK";
	public static final String GENERELL_SAK = "GENERELL_SAK";
	public static final String FAGSAK_ID = "0123A21";
	public static final String FAGSAKSYSTEM = "IT01";
	public static final String TEMA = "SYK";

	@ParameterizedTest
	@CsvSource(value = {
			FAGSAK + ", " + FAGSAK_ID + ", " + FAGSAKSYSTEM, //Ved fagsak skal fagsakID og fagsaksystem være satt
			GENERELL_SAK + ",," // ved generell_sak skal hverken fagsak eller fagsakID være satt
	})
	public void knyttTilAnnenSakHappyPath(String sakstype, String fagsakId, String fagsaksystem) {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);

		stubAzure();
		restStsToken();
		happyAktoerIdStub();
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponseKildeJournalpostId1-happy.json")));

		Long journalpostId = journalpostTestRepository.persist(createJournalpostWithHoveddokument()).getJournalpostId();
		commitAndStartNewTransaction();

		HttpEntity<KnyttTilAnnenSakRequest> requestEntity = new HttpEntity<>(createKnyttTilAnnenSakRequestHappyPath(sakstype, fagsakId, fagsaksystem, List.of()), createHeadersWithUserAndServiceUserToken());
		ResponseEntity<KnyttTilAnnenSakResponse> response = restTemplate.exchange(apiJournalpostPath(journalpostId + KNYTT_TIL_ANNEN_SAK), PUT, requestEntity, KnyttTilAnnenSakResponse.class);
		assertEquals(OK, response.getStatusCode());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Journalpost journalpost = journalpostTestRepository.findById(response.getBody().getNyJournalpostId()).orElseThrow(RuntimeException::new);

		assertNotNull(response);

		assertEquals(AvsenderMottakerIdTypeCode.FNR, journalpost.getAvsenderMottakerIdType());
		assertEquals(AVSENDER_MOTTAKER_NAVN, journalpost.getAvsenderMottaker());
		assertEquals(AVSENDER_MOTTAKER_ID, journalpost.getAvsenderMottakerId());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertEquals(4, aksjonsLoggList.size());
		valdiateAksjonsloggELement(aksjonsLoggList.get(0), KOPIER_JOURNALPOST, journalpostId, "Z990782");
		valdiateAksjonsloggELement(aksjonsLoggList.get(1), ENDRE_METADATA, journalpost.getJournalpostId(), "Z990782");
		valdiateAksjonsloggELement(aksjonsLoggList.get(2), SAKSTILKNYTNING, journalpost.getJournalpostId(), "Z990782");
		valdiateAksjonsloggELement(aksjonsLoggList.get(3), AksjonsTypeCode.FERDIGSTILL, journalpost.getJournalpostId(), "srvjoarkadmin");

		verify(exactly(1), postRequestedFor(urlEqualTo("/safgraphql"))
				.withRequestBody(equalToJson(String.format("""
						{
						  "query" : "query journalpost($queryJournalpostId: String!) {\\n\\t  journalpost(journalpostId: $queryJournalpostId) {\\n\\t\\tdokumenter {\\n\\t\\t  dokumentInfoId\\n\\t\\t  dokumentvarianter {\\n\\t\\t\\tsaksbehandlerHarTilgang\\n\\t\\t\\tvariantformat\\n\\t\\t  }\\n\\t\\t}\\n\\t  }\\n\\t}\\n",
						  "operationName": "journalpost",
						  "variables": {
						    "queryJournalpostId": "%s"
						  }
						}""", journalpostId)))
				.withHeader("Nav-Callid", matching(NAV_CALL_ID)));
	}

	@Test
	void shouldCopyDokumenterWhenReqestContainsDokumenter() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubAzure();
		restStsToken();
		happyAktoerIdStub();

		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpost.addJournalpostDokumentInfoRelasjon(createDokumentInfoVedleggRelasjon(journalpost));
		journalpost.addJournalpostDokumentInfoRelasjon(createDokumentInfoVedleggRelasjon(journalpost));

		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		List<Long> dokumenter = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(VEDLEGG).stream()
				.map(JournalpostDokumentInfoRelasjon::getDokumentInfo)
				.map(DokumentInfo::getDokumentInfoId)
				.toList();

		stubSafForDokumenter(dokumenter);

		HttpEntity<KnyttTilAnnenSakRequest> requestEntity = new HttpEntity<>(createKnyttTilAnnenSakRequestHappyPath(FAGSAK, FAGSAK_ID, FAGSAKSYSTEM, dokumenter), createHeadersWithUserAndServiceUserToken());
		ResponseEntity<KnyttTilAnnenSakResponse> response = restTemplate.exchange(apiJournalpostPath(journalpostId + KNYTT_TIL_ANNEN_SAK), PUT, requestEntity, KnyttTilAnnenSakResponse.class);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getNyJournalpostId()).isNotNull();

		var nyJournalpostId = response.getBody().getNyJournalpostId();

		commitAndStartNewTransaction();

		var nyJournalpost = journalpostTestRepository.findById(nyJournalpostId);

		var hoveddokumentId = dokumenter.getFirst();
		var vedleggId = dokumenter.getLast();

		// Sjekk at de to dokumentene fra requesten blir kopiert til den nye journalposten, som henholdsvis hoveddokument (første element) og vedlegg (siste element)
		assertThat(nyJournalpost)
				.isPresent()
				.get()
				.satisfies(jp ->
					assertThat(jp.getJournalpostDokumentInfoRelasjoner())
							.hasSize(2)
							.extracting(JournalpostDokumentInfoRelasjon::getTilknyttetJournalpostSom,
									rel -> rel.getDokumentInfo().getDokumentInfoId())
							.containsExactlyInAnyOrder(
									tuple(HOVEDDOKUMENT, hoveddokumentId),
									tuple(VEDLEGG, vedleggId)
							)
				);
	}

	@Test
	void shouldReturnBadRequestWhenDokumenterContainsDuplicates() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubAzure();

		HttpEntity<KnyttTilAnnenSakRequest> requestEntity = new HttpEntity<>(createKnyttTilAnnenSakRequestHappyPath(FAGSAK, FAGSAK_ID, FAGSAKSYSTEM, List.of(12345678L, 12345678L)), createHeadersWithUserAndServiceUserToken());
		ResponseEntity<KnyttTilAnnenSakResponse> response = restTemplate.exchange(apiJournalpostPath(JOURNALPOST_ID + KNYTT_TIL_ANNEN_SAK), PUT, requestEntity, KnyttTilAnnenSakResponse.class);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);

		//Feilmeldinger returneres ikke til konsument slik det er implementert nå, det bør sjekkes her når dette er utbedret
		//assertThat(response.getBody()).isEqualTo("Validering feilet for journalpostId=%s. Feilmelding=%s".formatted(JOURNALPOST_ID, feilmelding));
	}

	@Test
	void shouldReturnBadRequestWhenDokumenterDoesNotExist() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);

		stubAzure();
		restStsToken();
		happyAktoerIdStub();
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponseKildeJournalpostId1-happy.json")));

		Long journalpostId = journalpostTestRepository.persist(createJournalpostWithHoveddokument()).getJournalpostId();
		commitAndStartNewTransaction();

		HttpEntity<KnyttTilAnnenSakRequest> requestEntity = new HttpEntity<>(createKnyttTilAnnenSakRequestHappyPath(FAGSAK, FAGSAK_ID, FAGSAKSYSTEM, List.of(11111111L)), createHeadersWithUserAndServiceUserToken());
		ResponseEntity<KnyttTilAnnenSakResponse> response = restTemplate.exchange(apiJournalpostPath(journalpostId + KNYTT_TIL_ANNEN_SAK), PUT, requestEntity, KnyttTilAnnenSakResponse.class);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	@ParameterizedTest
	@CsvSource(value = {
			GENERELL_SAK + ", " + FAGSAK_ID + ", " + FAGSAKSYSTEM + ", fagsakId og fagsaksystem skal ikke oppgis dersom sakstype=GENERELL_SAK",
			GENERELL_SAK + ",, " + FAGSAKSYSTEM + ", fagsakId og fagsaksystem skal ikke oppgis dersom sakstype=GENERELL_SAK",
			GENERELL_SAK + ", " + FAGSAK_ID + ",, fagsakId og fagsaksystem skal ikke oppgis dersom sakstype=GENERELL_SAK",
			FAGSAK + ",,,fagsakId kan ikke være null eller tom dersom sakstype=FAGSAK",
			FAGSAK + "," + FAGSAK_ID + ",,fagsaksystem kan ikke være null eller tom dersom sakstype=FAGSAK",
			FAGSAK + ",," + FAGSAKSYSTEM + ",fagsakId kan ikke være null eller tom dersom sakstype=FAGSAK"
	})
	public void knyttTilAnnenSakShouldFailWithBadInput(String sakstype, String fagsakId, String fagsaksystem, String feilmelding) {
		HttpEntity<KnyttTilAnnenSakRequest> requestEntity = new HttpEntity<>(createKnyttTilAnnenSakRequestHappyPath(sakstype, fagsakId, fagsaksystem, List.of()), createHeadersWithUserAndServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(apiJournalpostPath("12345678910" + KNYTT_TIL_ANNEN_SAK), PUT, requestEntity, String.class);
		assertTrue(response.getBody().contains(feilmelding));
		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
	}

	@Test
	public void knyttTilAnnenSakJournalpostNotFound() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);

		stubAzure();
		restStsToken();
		happyAktoerIdStub();

		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponseJournalpostIkkeFunnet.json")));

		HttpEntity<KnyttTilAnnenSakRequest> requestEntity = new HttpEntity<>(createKnyttTilAnnenSakRequestHappyPath(), createHeadersWithUserAndServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(apiJournalpostPath(JOURNALPOST_ID + KNYTT_TIL_ANNEN_SAK), PUT, requestEntity, String.class);
		assertEquals(NOT_FOUND, response.getStatusCode());

		verify(exactly(1), postRequestedFor(urlEqualTo("/safgraphql")));
	}

	private void valdiateAksjonsloggELement(AksjonsLogg aksjonsloggElement, AksjonsTypeCode aksjonsType, long journalpostId, String utfoertAv) {
		assertEquals(utfoertAv, aksjonsloggElement.getUtfoertAv());
		assertEquals(aksjonsType, aksjonsloggElement.getAksjon());
		assertEquals(journalpostId, aksjonsloggElement.getJournalpostId());
	}

	public static KnyttTilAnnenSakRequest createKnyttTilAnnenSakRequestHappyPath() {
		return createKnyttTilAnnenSakRequest(FAGSAK, FAGSAK_ID, FAGSAKSYSTEM, TEMA, FNR, GYLDIG_FNR, JOURNALFOERENDE_ENHET, List.of());
	}

	public static KnyttTilAnnenSakRequest createKnyttTilAnnenSakRequestHappyPath(String sakstype, String fagsakid, String fagsaksystem, List<Long> dokumenter) {
		return createKnyttTilAnnenSakRequest(sakstype, fagsakid, fagsaksystem, TEMA, FNR, GYLDIG_FNR, JOURNALFOERENDE_ENHET, dokumenter);
	}

	@SneakyThrows
	private void stubSafForDokumenter(List<Long> dokumenter) {

		List<Map<String, Object>> dokumentliste = dokumenter.stream()
				.map(dokumentInfoId -> {
					Map<String, Object> dokument = new HashMap<>();
					dokument.put("dokumentInfoId", dokumentInfoId);
					dokument.put("dokumentvarianter", List.of(
							Map.of("saksbehandlerHarTilgang", true, "variantformat", ARKIV.name())
					));
					return dokument;})
				.toList();

		Map<String, Object> responseMap = Map.of(
				"data", Map.of(
						"journalpost", Map.of(
								"dokumenter", dokumentliste
						)
				)
		);

		String jsonResponse = new ObjectMapper().writeValueAsString(responseMap);

		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(jsonResponse)));
	}

}
