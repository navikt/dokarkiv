package no.nav.dokarkiv.core.security;

import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ValidateAdminConsumerAccessInterceptorTest {

	private static final String ADGRUPPE_JOARK_VEDLIKEHOLD = "0000-GA-joark-vedlikehold";

	private final AzureAdGraphService azureAdGraphService = mock(AzureAdGraphService.class);
	private final ValidateAdminConsumerAccessInterceptor validateAdminConsumerAccessInterceptor = new ValidateAdminConsumerAccessInterceptor(azureAdGraphService, ADGRUPPE_JOARK_VEDLIKEHOLD);

	@Test
	public void shouldDenyAccessIfAuthorizationHeaderIsNotSet() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		boolean result = validateAdminConsumerAccessInterceptor.preHandle(request, response, new Object());

		assertThat(result).isFalse();
		assertThat(response.getErrorMessage()).isEqualTo("Authorization-header må være satt");
	}

}