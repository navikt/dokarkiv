package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.JournalpostConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import wiremock.com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
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
	static final String KOPIER_QUERY = "kopierJournalpost?kildeJournalpostId=";
	static final String FERDIGSTILL_QUERY = "?forsoekFerdigstill=true";

	protected String OIDC_TOKEN_PERSON_USER_TEST;
	protected String OIDC_TOKEN_SERVICE_USER_TEST;

	@Configuration
	public static class Config {
		@Bean
		public AzureAdGraphService azureAdGraphService() {
			AzureAdGraphService azureAdGraphServiceMock = mock(AzureAdGraphService.class);
			when(azureAdGraphServiceMock.hentFulltNavn(any())).thenReturn(PERSON_USER_NAME);
			return azureAdGraphServiceMock;
		}
	}

	void abacPermit() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));

	}

	void restStsToken() {
		stubFor(post(urlEqualTo("/reststs"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("reststs/reststs-happy.json")));
	}

	void happyPersonIdentStub() {
		stubFor(post(urlEqualTo("/pdl"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/pdl-hentperson-happy.json")));
	}

	void happyFnrIdentStub() {
		stubFor(post(urlEqualTo("/pdl"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/pdl-folkeregisterident-happy.json")));
	}

	void identNotFoundStub() {
		stubFor(post(urlEqualTo("/pdl"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/pdl-ident-notfound.json")));
	}

	void happyAktoerIdStub() {
		stubFor(post(urlEqualTo("/pdl"))
				.willReturn(aResponse().withStatus(OK.value())
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
			return Resources.toString(url, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Could not convert url to String" + url);
		}
	}

	protected Journalpost buildAndCommit(final JournalpostBuilder builder) {
		Journalpost journalpost = journalpostTestRepository.persist(builder.build());
		TestTransaction.flagForCommit();
		TestTransaction.end();
		return journalpost;
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
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, BEARER + OIDC_TOKEN_PERSON_USER_TEST);
		headers.add(NAV_CONSUMER_TOKEN, BEARER + OIDC_TOKEN_SERVICE_USER_TEST);
		return headers;
	}

}
