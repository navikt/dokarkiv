package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.JournalpostConfig;
import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.web.util.UriComponentsBuilder;
import wiremock.com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.CollectionUtils.toMultiValueMap;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

@SpringBootTest(
		webEnvironment = RANDOM_PORT,
		classes = {CoreConfig.class, JournalpostConfig.class},
		properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles({"itest", "wiremock"})
@AutoConfigureWireMock(port = 0)
public abstract class AbstractJournalpostIT extends AbstractRestIT {

	static final String JOURNALPOSTAPI_BASE_PATH = "/rest/journalpostapi/v1/";
	static final String JOURNALPOSTAPI_JOURNALPOST_PATH = "journalpost";
	static final String JOURNALPOSTAPI_DOKUMENTINFO_PATH = "dokumentInfo";
	static final String JOURNALPOSTAPI_MOTTATTEJOURNALPOSTER_PATH = "finnMottatteJournalposter";
	static final String INTERNAL_JOURNALPOSTAPI_BASE_PATH = "/rest/internal/journalpostapi/v1/";
	static final String INTERNAL_JOURNALPOSTAPI_JOURNALPOST_PATH = "journalpost";
	static final String FERDIGSTILL = "/ferdigstill";
	protected static final String NAV_IDENT_SAKSBEHANDLER = "Z990782";

	protected String OIDC_TOKEN_PERSON_USER_TEST;
	protected String OIDC_TOKEN_SERVICE_USER_TEST;

	protected static String apiPath(String path) {
		return apiPathBuilder(path).build().toUriString();
	}

	protected static String apiPath(Map<String, List<String>> queryParams, String path) {
		UriComponentsBuilder builder = apiPathBuilder(path);
		if (queryParams.isEmpty()) {
			return builder.build().toUriString();
		} else {
			return builder.queryParams(toMultiValueMap(queryParams)).build().toUriString();
		}
	}

	protected static String apiJournalpostPath() {
		return apiJournalpostPath(Map.of(), "");
	}

	protected static String apiJournalpostPath(String... path) {
		return apiJournalpostPath(Map.of(), path);
	}

	protected static String apiJournalpostPath(Map<String, List<String>> queryParams) {
		return apiJournalpostPath(queryParams, "");
	}

	protected static String apiJournalpostPath(Map<String, List<String>> queryParams, String... path) {
		UriComponentsBuilder builder = apiPathBuilder(JOURNALPOSTAPI_JOURNALPOST_PATH).pathSegment(path);
		if (queryParams.isEmpty()) {
			return builder.build().toUriString();
		} else {
			return builder.queryParams(toMultiValueMap(queryParams)).build().toUriString();
		}
	}

	protected static String apiDokumentInfoPath(String... path) {
		return apiPathBuilder(JOURNALPOSTAPI_DOKUMENTINFO_PATH).pathSegment(path).build().toUriString();
	}

	protected static String apiMottatteJournalposterfoPath() {
		return apiPathBuilder(JOURNALPOSTAPI_MOTTATTEJOURNALPOSTER_PATH).build().toUriString();
	}

	protected static String apiInternalJournalpostPath(String... path) {
		return apiInternalPathBuilder(INTERNAL_JOURNALPOSTAPI_JOURNALPOST_PATH).pathSegment(path).build().toUriString();
	}

	private static UriComponentsBuilder apiPathBuilder(String path) {
		return fromPath(JOURNALPOSTAPI_BASE_PATH).path(path);
	}

	private static UriComponentsBuilder apiInternalPathBuilder(String... path) {
		return fromPath(INTERNAL_JOURNALPOSTAPI_BASE_PATH)
				.pathSegment(path);
	}

	void restStsToken() {
		stubFor(post(urlEqualTo("/reststs"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("reststs/reststs-happy.json")));
	}

	void happyPersonIdentStub() {
		stubFor(post(urlEqualTo("/pdl"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/pdl-hentperson-happy.json")));
	}

	void happyFnrIdentStub() {
		stubFor(post(urlEqualTo("/pdl"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/pdl-folkeregisterident-happy.json")));
	}

	void identNotFoundStub() {
		stubFor(post(urlEqualTo("/pdl"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/pdl-ident-notfound.json")));
	}

	public void happyAktoerIdStub() {
		stubFor(post(urlEqualTo("/pdl"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/pdl-aktoerid-happy.json")));
	}

	public void happyAktoerIdHistoriskStub() {
		stubFor(post(urlEqualTo("/pdl"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/pdl-aktoerid-happy-historisk.json")));
	}

	public void happyEregOrganisasjonStub() {
		stubFor(get(urlEqualTo("/ereg/123456789/noekkelinfo"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("ereg/ereg-organisasjon-happy.json")));
	}

	public void notFoundEregOrganisasjonStub() {
		stubFor(get(urlEqualTo("/ereg/123456789/noekkelinfo"))
				.willReturn(aResponse()
						.withStatus(NOT_FOUND.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("ereg/not_found_error.json")));
	}

	public void stubAzure() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

	public static String classpathToString(String path) {
		return resourceUrlToString(Resources.getResource(path));
	}

	public static String resourceUrlToString(URL url) {
		try {
			return Resources.toString(url, UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Could not convert url to String" + url);
		}
	}

	protected Journalpost buildAndCommit(final JournalpostBuilder builder) {
		Journalpost journalpost = journalpostTestRepository.persist(builder.build());
		commitAndStartNewTransaction();

		return journalpost;
	}

	protected Journalpost buildAndCommit(final Journalpost journalpost) {
		Journalpost nyJournalpost = journalpostTestRepository.persist(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		return nyJournalpost;
	}

	protected void clearSakRepository() {
		sakTestRepository.deleteAll();
		commitAndStartNewTransaction();
	}

	protected void commitAndStartNewTransaction() {
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

	protected HttpHeaders oidcHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.add(AUTHORIZATION, BEARER + OIDC_TOKEN_PERSON_USER_TEST);
		headers.add(NAV_CONSUMER_TOKEN, BEARER + OIDC_TOKEN_SERVICE_USER_TEST);
		return headers;
	}
}