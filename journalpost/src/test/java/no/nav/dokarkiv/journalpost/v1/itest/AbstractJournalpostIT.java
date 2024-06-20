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
import wiremock.com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(
		webEnvironment = RANDOM_PORT,
		classes = {CoreConfig.class, JournalpostConfig.class},
		properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles({"itest", "wiremock"})
@AutoConfigureWireMock(port = 0)
public abstract class AbstractJournalpostIT extends AbstractRestIT {

	static final String URL_JOURNALPOST = "/rest/journalpostapi/v1/journalpost/";
	static final String URL_BULK_DISTRIBUSJONSINFO_JOURNALPOST = "/rest/journalpostapi/v1/bulkOppdaterDistribusjonsinfo";
	static final String URL_DOKUMENTINFO = "/rest/journalpostapi/v1/dokumentInfo/";
	static final String URL_PROTECTED_INTERN = "/rest/internal/journalpostapi/v1/";
	static final String URL_PROTECTED_INTERN_JOURNALPOST = "/rest/internal/journalpostapi/v1/journalpost/";
	static final String FERDIGSTILL = "/ferdigstill";
	static final String KOPIER_QUERY = "kopierJournalpost?kildeJournalpostId={kildeJournalpostId}";
	static final String FERDIGSTILL_QUERY = "?forsoekFerdigstill=true";
	protected static final String MS_ID_SAKSBEHANDLER = "a123c63a-9821-4637-a23d-b706e5b24809";
	protected static final String NAV_IDENT_SAKSBEHANDLER = "Z990782";

	protected String OIDC_TOKEN_PERSON_USER_TEST;
	protected String OIDC_TOKEN_SERVICE_USER_TEST;

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

	void happyAktoerIdStub() {
		stubFor(post(urlEqualTo("/pdl"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/pdl-aktoerid-happy.json")));
	}

	void stubAzure() {
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

	protected static void stubMsGraphGetUser(String navIdent) {
		stubFor(get("/msgraph/users?$count=true&$filter=onPremisesSamAccountName%20eq%20%27" + navIdent + "%27&$select=givenname,surname")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("nav/msgraph-users.json")));
	}

	protected static void stubMsGraphMemberOfEgenAnsatt(String msUserId) {
		stubMsGraphMemberOf(msUserId, "nav/msgraph-memberof-egenansatt.json");
	}

	protected static void stubMsGraphMemberOfNotEgenAnsatt(String msUserId) {
		stubMsGraphMemberOf(msUserId, "nav/msgraph-memberof-not-egenansatt.json");
	}

	protected static void stubMsGraphMemberOf(String msUserId, String bodyFile) {
		stubFor(get("/msgraph/users/" + msUserId + "/memberOf")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile(bodyFile)));
	}
}