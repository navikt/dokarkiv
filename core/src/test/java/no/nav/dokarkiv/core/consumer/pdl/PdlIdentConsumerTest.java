package no.nav.dokarkiv.core.consumer.pdl;

import no.nav.dokarkiv.core.NaisProperties;
import no.nav.dokarkiv.core.consumer.texas.NaisTexasConsumer;
import no.nav.dokarkiv.core.consumer.texas.NaisTexasRequestInterceptor;
import no.nav.dokarkiv.core.properties.DokarkivProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PdlIdentConsumerTest {

	protected PdlIdentConsumer pdlIdentConsumer;
	private static MockWebServer mockServer;
	private RestClient restClient;

	@BeforeAll
	static void setupServer() throws IOException {
		mockServer = new MockWebServer();
		mockServer.start();
	}

	@BeforeEach
	public void intialize() {

		restClient = RestClient.builder().baseUrl(String.format("http://localhost:%s", mockServer.getPort())).build();

		pdlIdentConsumer = new PdlIdentConsumer(restClient, null, dokarkivProperties());
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

	private DokarkivProperties dokarkivProperties() {
		DokarkivProperties dokarkivProperties = new DokarkivProperties();
		DokarkivProperties.AzureEndpoint pdl = new DokarkivProperties.AzureEndpoint();
		pdl.setScope("api://cluster.namespace.pdl-api/.default");
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
