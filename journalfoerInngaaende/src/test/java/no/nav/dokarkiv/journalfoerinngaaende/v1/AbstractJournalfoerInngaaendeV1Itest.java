package no.nav.dokarkiv.journalfoerinngaaende.v1;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.ldap.AutoConfigureDataLdap;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import wiremock.com.google.common.io.Resources;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {CoreConfig.class, JournalfoerInngaaendeConfig.class})
@ActiveProfiles("itest,wiremock,ldap")
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@AutoConfigureDataLdap
@AutoConfigureWireMock(port = 0)
@Transactional
public abstract class AbstractJournalfoerInngaaendeV1Itest {

	protected static final String OIDC_TOKEN_PERSON_USER_TEST = "Bearer eyAidHlwIjogIkpXVCIsICJraWQiOiAiU0gxSWVSU2sxT1VGSDNzd1orRXVVcTE5VHZRPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICJLWENReU1JdUNHSkRaTzF3el9LM0d3IiwgInN1YiI6ICJaOTkwNzgyIiwgImF1ZGl0VHJhY2tpbmdJZCI6ICJmOTMzZTgxMy00ZDU5LTRjYjgtYTQ0OC0zMTliY2JlOWIzNTgtMjA0NzIwIiwgImlzcyI6ICJodHRwczovL2lzc28tdC5hZGVvLm5vOjQ0My9pc3NvL29hdXRoMiIsICJ0b2tlbk5hbWUiOiAiaWRfdG9rZW4iLCAiYXVkIjogImlkYS10IiwgImNfaGFzaCI6ICJRNzVsekZVanFlV09pZzNMdWxYOHlRIiwgIm9yZy5mb3JnZXJvY2sub3BlbmlkY29ubmVjdC5vcHMiOiAiMjg4NGFjY2MtYmU4My00MWFkLTk4NTctMWE2MWIyMDIzMTRkIiwgImF6cCI6ICJpZGEtdCIsICJhdXRoX3RpbWUiOiAxNTM1NDY0NDE4LCAicmVhbG0iOiAiLyIsICJleHAiOiAxNTM1NDY4MDE5LCAidG9rZW5UeXBlIjogIkpXVFRva2VuIiwgImlhdCI6IDE1MzU0NjQ0MTkgfQ.K9gDJI97u0A2mbF51qaS66AlXcVdzYYrIoUTXQ-Ol3nOdZ_XAEPSoQLi_uuccaniXZVjGCAOXXNuqdz9A-tY22cbiZ4SZ8HaSIA3WvRUOneES0r2RFg5oN3EAgt3okOHIShkPPjk7UwXqYe4D3dzZE6xaM7UmNMzyetvE4RMcti33bpXevonMxd-qHjWC9MuZBQdPwHvxIYgah0VGSp7WJ4KdizSW3ArPCWgZH-2UDvW8ugFVOigIOcEa93I3_HrBj6dTrlhn43WBo0q0G-Zvu0-Zya3Xts1QkJbRqmc6hpLF2attIPpqw8nwQv3S-gJidx_pLnPHK2OjjQgnMJruw";
	protected static final String OIDC_TOKEN_SERVICE_USER_TEST = "Bearer eyAidHlwIjogIkpXVCIsICJraWQiOiAiU0gxSWVSU2sxT1VGSDNzd1orRXVVcTE5VHZRPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICJjWmNWUHFMaURXSzFoTjhRN3RfT0RBIiwgInN1YiI6ICJzcnZkb2thcmtpdiIsICJhdWRpdFRyYWNraW5nSWQiOiAiZjg0ODIxYTktOGZkZS00OTI2LThlYmYtMWZiOTlkMzY5MjE2LTI3MjI3MiIsICJpc3MiOiAiaHR0cHM6Ly9pc3NvLXQuYWRlby5ubzo0NDMvaXNzby9vYXV0aDIiLCAidG9rZW5OYW1lIjogImlkX3Rva2VuIiwgImF1ZCI6ICJmcmVnLXRva2VuLXByb3ZpZGVyLXQwIiwgImNfaGFzaCI6ICIwakloUXd3NnVwU2tnSmY5U1RpemZRIiwgIm9yZy5mb3JnZXJvY2sub3BlbmlkY29ubmVjdC5vcHMiOiAiZTYzZjM4MTUtZTI0OS00Y2RmLTllMDUtYTY4NDc2YzdjYzhmIiwgImF6cCI6ICJmcmVnLXRva2VuLXByb3ZpZGVyLXQwIiwgImF1dGhfdGltZSI6IDE1MzU1Mzg2NzEsICJyZWFsbSI6ICIvIiwgImV4cCI6IDE1MzU1NDIyNzEsICJ0b2tlblR5cGUiOiAiSldUVG9rZW4iLCAiaWF0IjogMTUzNTUzODY3MSB9.rX5trZihldaIny2H9ePl0PoLGR9hPLYogdbnNv68bkRn-5jgX1OsQO3S9hCFUzq4C7jjfVB03aI6Xbx_0SMwf01hrBmQeTGBTLimer_b_rdA6fLxzwc2yek94GhBLwh9hkOyAtHjD4blShag-rxJnE0sgGwTUZ5hqDPRZWPJl9rnCoIBoaLd8qMQLltdy9Wzr_1w1jb8CZOM8gGY-k7jrMlS4ddxZHrQTQhIzUcsEMNRRZW8QlhmGtn-TRPlnYGSoP0HO2oSnMtF5fnw0ui_eQ-Xawy_qojB5RrxqM_-0UMVkHfvWhdDBg6DR4zS1UzbqIidJfHRdu7cidCp7OkA2w";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Inject
	protected JoarkRepository joarkRepository;
	@Inject
	protected TestRestTemplate restTemplate;
	@Inject
	protected DokumentinfoRepository dokumentinfoRepository;

	@Autowired
	private TestEntityManager testEntityManager;

	@Before
	public void setUp() {
		MDC.put(MDCConstants.MDC_USER_ID, "itest_userId");
		MDC.put(MDCConstants.MDC_CONSUMER_ID, "Itest_consumerId");
	}

	@BeforeClass
	public static void setupItest() {

		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("itestuser")
				.componentId("itest")
				.build());
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

	@Before
	public void cleanup() {
		joarkRepository.deleteAll();
		dokumentinfoRepository.deleteAll();
	}

	protected Journalpost buildAndCommit(final JournalpostBuilder builder) {
		Journalpost journalpost = joarkRepository.save(builder.build());
		TestTransaction.flagForCommit();
		TestTransaction.end();
		return journalpost;
	}

	protected HttpEntity createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_SERVICE_USER_TEST);
		return new HttpEntity(headers);
	}

	protected HttpHeaders oidcHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_SERVICE_USER_TEST);
		return headers;
	}

	protected void abacDeny() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("/abac/abac-deny.json")));
	}

	protected void abacPermit() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("/abac/abac-permit.json")));
	}

	protected String stringFromClasspath(String resourcename) throws IOException {
		return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(resourcename));
	}
}
