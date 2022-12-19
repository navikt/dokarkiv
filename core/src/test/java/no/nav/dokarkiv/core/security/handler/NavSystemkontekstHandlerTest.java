package no.nav.dokarkiv.core.security.handler;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;

import static no.nav.dokarkiv.core.security.handler.SelfSignedTokenFactory.createRestStsToken;
import static no.nav.dokarkiv.core.security.handler.SelfSignedTokenFactory.defaultRestStsClaimsSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NavSystemkontekstHandlerTest {

	private static final String APP_CLAIM_SUB = "srvskanmotreferanse";

	private static final String USER_NAVIDENT = "D999999";
	private static final String USER_NAME = "Donald Duck";

	private final AzureAdGraphService azureAdGraphService = mock(AzureAdGraphService.class);
	private final NavSystemkontekstHandler navSystemkontekstHandler = new NavSystemkontekstHandler(azureAdGraphService);

	@Test
	void shouldReturnFalseWhenHandledRestStsToken() throws Exception {
		boolean handle = navSystemkontekstHandler.handle(createRestStsToken(defaultRestStsClaimsSet(APP_CLAIM_SUB)), new MockHttpServletResponse(), null);
		assertThat(handle).isFalse();

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(APP_CLAIM_SUB);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_SUB);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(APP_CLAIM_SUB);
	}

	@Test
	void shouldReturnFalseWhenHandledRestStsTokenWithNavUserIdHeader() throws Exception {
		when(azureAdGraphService.hentFulltNavn(USER_NAVIDENT)).thenReturn(USER_NAME);
		boolean handle = navSystemkontekstHandler.handle(createRestStsToken(defaultRestStsClaimsSet(APP_CLAIM_SUB)), new MockHttpServletResponse(), USER_NAVIDENT);
		assertThat(handle).isFalse();

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(USER_NAVIDENT);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_SUB);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(USER_NAME);
	}

	@Test
	void shouldSetServiceuserContextWhenHandledRestStsTokenAndInvalidNavIdentFormat() throws Exception {
		when(azureAdGraphService.hentFulltNavn(USER_NAVIDENT)).thenReturn(USER_NAME);
		boolean handle = navSystemkontekstHandler.handle(createRestStsToken(defaultRestStsClaimsSet(APP_CLAIM_SUB)), new MockHttpServletResponse(), "DD99999");
		assertThat(handle).isFalse();

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(APP_CLAIM_SUB);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_SUB);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(APP_CLAIM_SUB);
	}

	@Test
	void shouldSetServiceuserContextWhenHandledRestStsTokenAndNavIdentNotFoundInLdap() throws Exception {
		when(azureAdGraphService.hentFulltNavn("Z111111")).thenReturn(null);
		boolean handle = navSystemkontekstHandler.handle(createRestStsToken(defaultRestStsClaimsSet(APP_CLAIM_SUB)), new MockHttpServletResponse(), "Z111111");
		assertThat(handle).isFalse();

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(APP_CLAIM_SUB);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_SUB);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(APP_CLAIM_SUB);
	}

	@Test
	void shouldReturnTrueWhenHandledRestStsTokenNoServiceUser() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		boolean handle = navSystemkontekstHandler.handle(createRestStsToken(defaultRestStsClaimsSet(USER_NAVIDENT)), response, null);
		assertThat(handle).isTrue();
		assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
	}
}