package no.nav.dokarkiv;

import jakarta.transaction.Transactional;
import no.nav.dokarkiv.core.exceptions.ApplicationProblemDetail;
import no.nav.dokarkiv.core.exceptions.ApplicationServletExceptionHandler;
import no.nav.dokarkiv.core.springdoc.SpringdocConfig;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.http.RequestEntity.patch;
import static org.springframework.http.RequestEntity.post;

@SpringBootTest(classes = {Application.class},
		webEnvironment = RANDOM_PORT,
		properties = {"springdoc.enabled=true"})
@ActiveProfiles("itest")
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@EnableMockOAuth2Server
@Transactional
public class ApplicationIT {

	private static final String LIVENESS_PATH = "/actuator/health/liveness";
	private static final String JOURNALPOSTAPI_JOURNALPOST_PATH = "/rest/journalpostapi/v1/journalpost";
	private static final String EXPECTED_DETAIL = "Kunne ikke ferdigstille journalpost med journalpostId=200000000. Feltet journalfoerendeEnhet kan ikke være null eller tomt. journalfoerendeEnhet=null";
	private static final URI EXPECTED_INSTANCE = URI.create("/rest/journalpostapi/v1/journalpost/200000000/ferdigstill");

	@Autowired
	private TestRestTemplate testRestTemplate;
	@Autowired
	private MockOAuth2Server mockOAuth2Server;

	@Test
	void shouldStartApp() {
		// verifisere at appen klarer starte opp
		var liveness = testRestTemplate.getForEntity(LIVENESS_PATH, String.class);
		assertThat(liveness.getStatusCode()).isEqualTo(OK);
		assertThat(liveness.getBody()).contains("UP");
	}

	/**
	 * <a href="https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide#spring-mvc-and-webflux-url-matching-changes">Spring Boot 3.0 Migration Guide</a>
	 * Fjernes/endres etter at alle klienter i prod er tilpasset. Bruk Elastic APM for oversikt.
	 * Denne returnerer 404 Not Found som default
	 *
	 * @see no.nav.dokarkiv.core.security.RestWebMvcConfig
	 */
	@Test
	void shouldSupportTrailingSlash() {
		var response = testRestTemplate.exchange(post(JOURNALPOSTAPI_JOURNALPOST_PATH + "/")
				.headers(httpHeaders -> httpHeaders.setBearerAuth(token("azurev2", "itest", Map.of("oid", "itest"))))
				.build(), String.class);
		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
	}

	/**
	 * Har lagt til custom felt i ProblemDetail for å ikke knekke for mange klienter i overgangen fra spring boot 2 til spring boot 3
	 *
	 * @see ApplicationServletExceptionHandler
	 * @see ApplicationProblemDetail
	 */
	@Test
	void shouldSupportApplicationProblemDetail() {
		var response = testRestTemplate.exchange(patch(JOURNALPOSTAPI_JOURNALPOST_PATH + "/200000000/ferdigstill")
				.headers(httpHeaders -> {
					httpHeaders.setContentType(APPLICATION_JSON);
					httpHeaders.setBearerAuth(token("azurev2", "itest", Map.of("oid", "itest")));
				})
				.body("{}"), ApplicationProblemDetail.class);
		assertThat(response.getHeaders().getContentType()).isEqualTo(APPLICATION_PROBLEM_JSON);
		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
		ApplicationProblemDetail applicationProblemDetail = response.getBody();
		assertThat(applicationProblemDetail).isNotNull();
		assertThat(applicationProblemDetail.getTitle()).isEqualTo(BAD_REQUEST.getReasonPhrase());
		assertThat(applicationProblemDetail.getDetail()).isEqualTo(EXPECTED_DETAIL);
		assertThat(applicationProblemDetail.getInstance()).isEqualTo(EXPECTED_INSTANCE);
		// Dette er custom felter
		assertThat(applicationProblemDetail.getTimestamp()).isInThePast();
		assertThat(applicationProblemDetail.getError()).isEqualTo(BAD_REQUEST.getReasonPhrase());
		assertThat(applicationProblemDetail.getMessage()).isEqualTo(EXPECTED_DETAIL);
		assertThat(applicationProblemDetail.getPath()).isEqualTo(EXPECTED_INSTANCE);
	}

	/**
	 * Sjekk at SpringDoc er oppe og at den redirecter riktig
	 *
	 * @see SpringdocConfig
	 */
	@Test
	void shouldCheckSpringDoc() {
		var response = testRestTemplate.exchange(RequestEntity.get("/swagger-ui.html").build(), String.class);
		assertThat(response.getStatusCode()).isEqualTo(FOUND);
		assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create("/swagger-ui/index.html"));
	}

	private String token(String issuer, String subject, Map<String, Object> claims) {
		String audience = "aud-localhost";
		return mockOAuth2Server.issueToken(
				issuer,
				"dokarkiv-itest",
				new DefaultOAuth2TokenCallback(
						issuer,
						subject,
						"JWT",
						List.of(audience),
						claims,
						3600
				)
		).serialize();
	}
}