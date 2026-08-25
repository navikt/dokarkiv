package no.nav.dokarkiv.core.consumer.pdl;

import no.nav.dokarkiv.core.NaisProperties;
import no.nav.dokarkiv.core.consumer.texas.NaisTexasConsumer;
import no.nav.dokarkiv.core.consumer.texas.NaisTexasRequestInterceptor;
import no.nav.dokarkiv.core.exceptions.PdlTechnicalException;
import no.nav.dokarkiv.core.properties.DokarkivProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PdlIdentConsumerTest {

	protected PdlIdentConsumer pdlIdentConsumer;
	private MockWebServer mockServer;
	private RestClient restClient;

	@BeforeEach
	public void intialize() throws IOException {
		mockServer = new MockWebServer();
		mockServer.start();

		restClient = RestClient.builder()
			.baseUrl(String.format("http://localhost:%s", mockServer.getPort()))
			.requestFactory(shortTimeoutRequestFactory())
			.build();
		pdlIdentConsumer = new PdlIdentConsumer(restClient, new StubNaisTexasConsumer("test-token"), dokarkivProperties());
	}

	@AfterEach
	public void tearDown() throws IOException {
		mockServer.shutdown();
	}

	@Test
	public void shouldValidateFnrWith11Numbers() {
		String validatedIdent = pdlIdentConsumer.validateAndTrimIdent("11111111111");
		assertEquals("11111111111", validatedIdent);
	}

	@Test
	public void shouldValidateAktoerIdWith11NumbersWithSpace() {
		String validatedIdent = pdlIdentConsumer.validateAndTrimIdent("    11111111111    ");
		assertEquals("11111111111", validatedIdent);
	}

	@Test
	public void shouldValidateAktoerIdWith13Numbers() {
		String validatedIdent = pdlIdentConsumer.validateAndTrimIdent("1111111111111");
		assertEquals("1111111111111", validatedIdent);
	}

	@Test
	public void shouldThrowExceptionWhenFolkeregisterIdentIsCorrectLengthWithCharactersThatIsNotNumeric() {
		assertThrows(PersonIkkeFunnetException.class, () -> pdlIdentConsumer.validateAndTrimIdent("1test11test"));
	}

	@Test
	public void shouldThrowExceptionWhenFolkeregisterIdentIsNull() {
		assertThrows(PersonIkkeFunnetException.class, () -> pdlIdentConsumer.validateAndTrimIdent(null));
	}

	@Test
	public void shouldThrowExceptionWhenFolkeregisterIdentIsEmpty() {
		assertThrows(PersonIkkeFunnetException.class, () -> pdlIdentConsumer.validateAndTrimIdent(""));
	}

	@Test
	public void shouldThrowExceptionWhenFolkeregisterIdentIsNotNumeric() {
		assertThrows(PersonIkkeFunnetException.class, () -> pdlIdentConsumer.validateAndTrimIdent("abc"));
	}

	@Test
	public void shouldThrowExceptionWhenFolkeregisterIdentIsNotValidLength() {
		assertThrows(PersonIkkeFunnetException.class, () -> pdlIdentConsumer.validateAndTrimIdent("123"));
	}

	@Test
	public void shouldSetBearerAuthorizationHeaderFromNaisTexasSystemTokenUsingExplicitScopeNotAttributeBackedScope() throws InterruptedException {
		String expectedToken = "test-access-token";
		String notExpectedToken = "test-access-token-from-global";
		RestClient restClientWithTexasAuth = RestClient.builder()
			.baseUrl(String.format("http://localhost:%s", mockServer.getPort()))
			.requestFactory(shortTimeoutRequestFactory())
			.requestInterceptor(new NaisTexasRequestInterceptor(new StubNaisTexasConsumer(notExpectedToken)))
			.build();

		DokarkivProperties dokarkivProperties = dokarkivProperties();
		dokarkivProperties.getEndpoints().getPdl().setUrl(String.format("http://localhost:%s", mockServer.getPort()));
		PdlIdentConsumer consumerWithAuth = new PdlIdentConsumer(restClientWithTexasAuth, new StubNaisTexasConsumer(expectedToken), dokarkivProperties);

		mockServer.enqueue(new MockResponse()
			.addHeader("Content-Type", "application/json")
			.setBody("""
				{"data":{"hentIdenter":{"identer":[{"ident":"1111111111111","historisk":false,"gruppe":"AKTORID"}]}}}
				"""));

		consumerWithAuth.hentAktoerId("11111111111");

		RecordedRequest recordedRequest = mockServer.takeRequest();
		assertEquals("Bearer " + expectedToken, recordedRequest.getHeader("Authorization"));
	}

	@Test
	public void hentAktoerIdShouldReturnAktoerIdOnSuccess() {
		enqueueGraphQlResponse("""
			{"data":{"hentIdenter":{"identer":[{"ident":"1111111111111","historisk":false,"gruppe":"AKTORID"}]}}}
			""");

		String aktoerId = pdlIdentConsumer.hentAktoerId("11111111111");

		assertEquals("1111111111111", aktoerId);
	}

	@Test
	public void hentAktoerIdShouldReturnNullWhenIdenterListIsEmpty() {
		enqueueGraphQlResponse("""
			{"data":{"hentIdenter":{"identer":[]}}}
			""");

		assertNull(pdlIdentConsumer.hentAktoerId("11111111111"));
	}

	@Test
	public void hentAktoerIdShouldThrowPersonIkkeFunnetExceptionWhenPdlReturnsNotFoundError() {
		enqueueGraphQlResponse("""
			{"data":null,"errors":[{"message":"Fant ikke person","extensions":{"code":"not_found"}}]}
			""");

		assertThrows(PersonIkkeFunnetException.class, () -> pdlIdentConsumer.hentAktoerId("11111111111"));
	}

	@Test
	public void hentAktoerIdShouldThrowPdlFunctionalExceptionWhenPdlReturnsOtherGraphQlError() {
		enqueueGraphQlResponse("""
			{"data":null,"errors":[{"message":"Noe gikk galt","extensions":{"code":"server_error"}}]}
			""");

		assertThrows(PdlFunctionalException.class, () -> pdlIdentConsumer.hentAktoerId("11111111111"));
	}

	@Test
	public void hentAktoerIdShouldThrowPdlFunctionalExceptionOnHttp4xxResponse() {
		mockServer.enqueue(new MockResponse().setResponseCode(400));

		PdlFunctionalException exception = assertThrows(PdlFunctionalException.class,
			() -> pdlIdentConsumer.hentAktoerId("11111111111"));
		assertTrue(exception.getMessage().contains("400"));
	}

	@Test
	public void hentAktoerIdShouldThrowPdlTechnicalExceptionOnHttp5xxResponse() {
		mockServer.enqueue(new MockResponse().setResponseCode(500));

		assertThrows(PdlTechnicalException.class, () -> pdlIdentConsumer.hentAktoerId("11111111111"));
	}

	@Test
	public void hentFolkeregisterIdentShouldReturnIdentOnSuccess() {
		enqueueGraphQlResponse("""
			{"data":{"hentIdenter":{"identer":[{"ident":"11111111111","historisk":false,"gruppe":"FOLKEREGISTERIDENT"}]}}}
			""");

		String ident = pdlIdentConsumer.hentFolkeregisterIdent("1111111111111");

		assertEquals("11111111111", ident);
	}

	@Test
	public void hentFolkeregisterIdentShouldThrowPersonIkkeFunnetExceptionWhenPdlReturnsNotFoundError() {
		enqueueGraphQlResponse("""
			{"data":null,"errors":[{"message":"Fant ikke person","extensions":{"code":"not_found"}}]}
			""");

		assertThrows(PersonIkkeFunnetException.class, () -> pdlIdentConsumer.hentFolkeregisterIdent("1111111111111"));
	}

	@Test
	public void hentFolkeregisterIdentShouldThrowPdlFunctionalExceptionWhenPdlReturnsOtherGraphQlError() {
		enqueueGraphQlResponse("""
			{"data":null,"errors":[{"message":"Noe gikk galt","extensions":{"code":"server_error"}}]}
			""");

		assertThrows(PdlFunctionalException.class, () -> pdlIdentConsumer.hentFolkeregisterIdent("1111111111111"));
	}

	@Test
	public void hentFolkeregisterIdentShouldThrowPdlTechnicalExceptionOnHttp5xxResponse() {
		mockServer.enqueue(new MockResponse().setResponseCode(500));

		assertThrows(PdlTechnicalException.class, () -> pdlIdentConsumer.hentFolkeregisterIdent("1111111111111"));
	}

	@Test
	public void hentAlleAktoerIdsForIdentShouldReturnAllIdenterOnSuccess() {
		enqueueGraphQlResponse("""
			{"data":{"hentIdenter":{"identer":[
			  {"ident":"1111111111111","historisk":false,"gruppe":"AKTORID"},
			  {"ident":"2222222222222","historisk":true,"gruppe":"AKTORID"}
			]}}}
			""");

		List<String> identer = pdlIdentConsumer.hentAlleAktoerIdsForIdent("11111111111");

		assertEquals(List.of("1111111111111", "2222222222222"), identer);
	}

	@Test
	public void hentAlleAktoerIdsForIdentShouldThrowPersonIkkeFunnetExceptionWhenPdlReturnsNotFoundError() {
		enqueueGraphQlResponse("""
			{"data":null,"errors":[{"message":"Fant ikke person","extensions":{"code":"not_found"}}]}
			""");

		assertThrows(PersonIkkeFunnetException.class, () -> pdlIdentConsumer.hentAlleAktoerIdsForIdent("11111111111"));
	}

	@Test
	public void hentAlleAktoerIdsForIdentShouldThrowPdlFunctionalExceptionWhenPdlReturnsOtherGraphQlError() {
		enqueueGraphQlResponse("""
			{"data":null,"errors":[{"message":"Noe gikk galt","extensions":{"code":"server_error"}}]}
			""");

		assertThrows(PdlFunctionalException.class, () -> pdlIdentConsumer.hentAlleAktoerIdsForIdent("11111111111"));
	}

	@Test
	public void hentAlleAktoerIdsForIdentShouldThrowPdlFunctionalExceptionOnHttp4xxResponse() {
		mockServer.enqueue(new MockResponse().setResponseCode(404));

		assertThrows(PdlFunctionalException.class, () -> pdlIdentConsumer.hentAlleAktoerIdsForIdent("11111111111"));
	}

	@Test
	public void hentPersonnavnShouldReturnFulltNavnOnSuccess() {
		enqueueGraphQlResponse("""
			{"data":{"hentPerson":{"navn":[{"fornavn":"Ola","mellomnavn":null,"etternavn":"Nordmann"}]}}}
			""");

		String navn = pdlIdentConsumer.hentPersonnavn("11111111111");

		assertEquals("Ola Nordmann", navn);
	}

	@Test
	public void hentPersonnavnShouldThrowPersonIkkeFunnetExceptionWhenPdlReturnsNotFoundError() {
		enqueueGraphQlResponse("""
			{"data":{"hentPerson":null},"errors":[{"message":"Fant ikke person","extensions":{"code":"not_found"}}]}
			""");

		assertThrows(PersonIkkeFunnetException.class, () -> pdlIdentConsumer.hentPersonnavn("11111111111"));
	}

	@Test
	public void hentPersonnavnShouldThrowPdlFunctionalExceptionWhenPersonHasNoNavnAndNoErrors() {
		enqueueGraphQlResponse("""
			{"data":{"hentPerson":{"navn":[]}}}
			""");

		PdlFunctionalException exception = assertThrows(PdlFunctionalException.class,
			() -> pdlIdentConsumer.hentPersonnavn("11111111111"));
		assertTrue(exception.getMessage().contains("ikke navn"));
	}

	@Test
	public void hentPersonnavnShouldThrowPdlFunctionalExceptionWhenPdlReturnsOtherGraphQlError() {
		enqueueGraphQlResponse("""
			{"data":{"hentPerson":null},"errors":[{"message":"Noe gikk galt","extensions":{"code":"server_error"}}]}
			""");

		assertThrows(PdlFunctionalException.class, () -> pdlIdentConsumer.hentPersonnavn("11111111111"));
	}

	@Test
	public void hentPersonnavnShouldThrowPdlTechnicalExceptionOnHttp5xxResponse() {
		mockServer.enqueue(new MockResponse().setResponseCode(500));

		assertThrows(PdlTechnicalException.class, () -> pdlIdentConsumer.hentPersonnavn("11111111111"));
	}

	private void enqueueGraphQlResponse(String body) {
		mockServer.enqueue(new MockResponse()
			.addHeader("Content-Type", "application/json")
			.setBody(body));
	}

	private static HttpComponentsClientHttpRequestFactory shortTimeoutRequestFactory() {
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setConnectionRequestTimeout(Duration.ofSeconds(1));
		requestFactory.setReadTimeout(Duration.ofSeconds(1));
		return requestFactory;
	}

	private DokarkivProperties dokarkivProperties() {
		DokarkivProperties dokarkivProperties = new DokarkivProperties();
		DokarkivProperties.AzureEndpoint pdl = new DokarkivProperties.AzureEndpoint();
		pdl.setScope("api://cluster.namespace.pdl-api/.default");
		pdl.setUrl(String.format("http://localhost:%s", mockServer.getPort()));
		DokarkivProperties.Endpoints endpoints = new DokarkivProperties.Endpoints();

		dokarkivProperties.getEndpoints().setPdl(pdl);

		endpoints.setPdl(pdl);
		return dokarkivProperties;
	}

	private static class StubNaisTexasConsumer extends NaisTexasConsumer {
		private final String token;

		private StubNaisTexasConsumer(String token) {
			super(RestClient.builder(), new NaisProperties());
			this.token = token;
		}

		@Override
		public String getSystemToken(String targetScope) {
			return token;
		}
	}
}
