package no.nav.dokarkiv.core.consumer.pdl;

import no.nav.dokarkiv.core.properties.DokarkivProperties;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
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

		pdlIdentConsumer = new PdlIdentConsumer(
				restClient, dokarkivProperties());
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

	private DokarkivProperties dokarkivProperties() {
		DokarkivProperties dokarkivProperties = new DokarkivProperties();
		DokarkivProperties.AzureEndpoint pdl = new DokarkivProperties.AzureEndpoint();
		DokarkivProperties.Endpoints endpoints = new DokarkivProperties.Endpoints();

		dokarkivProperties.getEndpoints().setPdl(pdl);

		endpoints.setPdl(pdl);
		return dokarkivProperties;
	}
}