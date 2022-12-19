package no.nav.dokarkiv.core.security.handler;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static no.nav.dokarkiv.core.security.handler.SelfSignedTokenFactory.createAzureToken;
import static no.nav.dokarkiv.core.security.handler.SelfSignedTokenFactory.defaultAzureClaimSet;
import static no.nav.dokarkiv.core.security.handler.SelfSignedTokenFactory.defaultNavClaimSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureAdFlowSporingHandlerTest {
	private static final String APP_CLAIM_AZP = "a2fb96a7-5294-48ea-a1de-a30599f95eb4";
	private static final String APP_CLAIM_SUB = "a2fb96a7-5294-48ea-a1de-a30599f95eb4";
	private static final String APP_CLAIM_OID = "a2fb96a7-5294-48ea-a1de-a30599f95eb4";
	private static final String APP_CLAIM_AZP_NAME = "dev-fss:andeby:donald_duck";
	private static final String APP_CLAIM_AZP_NAME_PARSED = "andeby:donald_duck";
	private static final String APP_CLAIM_AZP_NAME_LANG = "dev-fss:flaaklypa:sjeik_ben_redic_fy_fazan_snake_oil";
	private static final String USER_CLAIM_OID = "52968c79-cd9c-4368-a871-8e2b07f4d8b9";
	private static final String USER_CLAIM_NAVIDENT = "P999999";
	private static final String USER_CLAIM_NAME = "Donald Duck";

	private final AzureAdGraphService azureAdGraphServiceMock = mock(AzureAdGraphService.class);
	private final AzureAdFlowSporingHandler azureAdFlowSporingHandler = new AzureAdFlowSporingHandler(azureAdGraphServiceMock);

	@Test
	void shouldHandleClientCredentialGrantFlowWhenAppTokenContainsIdTypClaim() {
		azureAdFlowSporingHandler.handle(createAzureClientCredentialGrantWithOptionalIdtypClaimToken(), null);

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(APP_CLAIM_AZP_NAME_PARSED);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_AZP_NAME_PARSED);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(APP_CLAIM_AZP_NAME_PARSED);
	}

	@Test
	void shouldHandleClientCredentialGrantFlowWhenAppTokenDoesNotContainIdTypClaim() {
		azureAdFlowSporingHandler.handle(createAzureClientCredentialGrantToken(), null);

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(APP_CLAIM_AZP_NAME_PARSED);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_AZP_NAME_PARSED);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(APP_CLAIM_AZP_NAME_PARSED);
	}

	@Test
	void shouldHandleClientCredentialGrantFlowWhenNavUserIdHeaderSet() {
		when(azureAdGraphServiceMock.hentFulltNavn(USER_CLAIM_NAVIDENT)).thenReturn(USER_CLAIM_NAME);
		azureAdFlowSporingHandler.handle(createAzureClientCredentialGrantToken(), USER_CLAIM_NAVIDENT);

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(USER_CLAIM_NAVIDENT);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_AZP_NAME_PARSED);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(USER_CLAIM_NAME);
	}

	@Test
	void shouldSetAppContextWhenNavUserIdHeaderSetAndNotExistsInLdap() {
		when(azureAdGraphServiceMock.hentFulltNavn(USER_CLAIM_NAVIDENT)).thenReturn(null);
		azureAdFlowSporingHandler.handle(createAzureClientCredentialGrantToken(), USER_CLAIM_NAVIDENT);

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(APP_CLAIM_AZP_NAME_PARSED);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_AZP_NAME_PARSED);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(APP_CLAIM_AZP_NAME_PARSED);
	}

	@Test
	void shouldSetAppContextWhenNavUserIdHeaderSetAndInvalidNavIdent() {
		azureAdFlowSporingHandler.handle(createAzureClientCredentialGrantToken(), "ZZ9999");

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(APP_CLAIM_AZP_NAME_PARSED);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_AZP_NAME_PARSED);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(APP_CLAIM_AZP_NAME_PARSED);
	}

	@Test
	void shouldHandleOnBehalfOfFlow() {
		azureAdFlowSporingHandler.handle(createAzureOnBehalfOfToken(), null);

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(USER_CLAIM_NAVIDENT);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_AZP_NAME_PARSED);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(USER_CLAIM_NAME);
	}

	@Test
	void shouldHandleOnBehalfOfFlowMissingNavIdent() {
		azureAdFlowSporingHandler.handle(createAzureOnBehalfOfTokenWithoutNavIdent(), null);

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(USER_CLAIM_OID);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_AZP_NAME_PARSED);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(USER_CLAIM_NAME);
	}

	@Test
	void shouldFallbackToAzpClaimWhenAzpnameClaimNotSetWhenClientCredentialFlow() {
		JwtToken azureTokenNoAzpnameClaim = createAzureToken(defaultAzureClaimSet(APP_CLAIM_SUB, APP_CLAIM_OID, APP_CLAIM_AZP));
		azureAdFlowSporingHandler.handle(azureTokenNoAzpnameClaim, null);

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(APP_CLAIM_AZP);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_AZP);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(APP_CLAIM_AZP);
	}

	@Test
	void shouldShortenAzpnameClaimTo40CharsWhenMoreThan40Chars() {
		JwtToken azureTokenAzpnameClaimMoreThan40Chars = createAzureToken(defaultNavClaimSet(APP_CLAIM_SUB, APP_CLAIM_OID, APP_CLAIM_AZP, APP_CLAIM_AZP_NAME_LANG, null));
		azureAdFlowSporingHandler.handle(azureTokenAzpnameClaimMoreThan40Chars, null);

		String expected = "flaaklypa:sjeik_ben_redic_fy_fazan_snake";
		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(expected);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(expected);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(expected);
	}

	@Test
	void shouldParseAzpnameWhenNotMatchingPattern() {
		JwtToken azureTokenMalformedAzpname = createAzureToken(defaultNavClaimSet(APP_CLAIM_SUB, APP_CLAIM_OID, APP_CLAIM_AZP, "¨dev-fss:gotham:batman", null));
		azureAdFlowSporingHandler.handle(azureTokenMalformedAzpname, null);

		String expected = "gotham:batman";
		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(expected);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(expected);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(expected);
	}

	@Test
	void shouldFailoverAndParseAzpnameWhenNoColons() {
		JwtToken azureTokenMalformedAzpname = createAzureToken(defaultNavClaimSet(APP_CLAIM_SUB, APP_CLAIM_OID, APP_CLAIM_AZP, "dev-fss;gotham;batman", null));
		azureAdFlowSporingHandler.handle(azureTokenMalformedAzpname, null);

		String expected = "dev-fss;gotham;batman";
		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(expected);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(expected);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(expected);
	}

	@Test
	void shouldThrowMissingClaimExceptionWhenAzureOnBehalfTokenIsMissingNameClaim() {
		assertThatThrownBy(() -> azureAdFlowSporingHandler.handle(createAzureToken(defaultNavClaimSet(APP_CLAIM_SUB, USER_CLAIM_OID, APP_CLAIM_AZP, APP_CLAIM_AZP_NAME, USER_CLAIM_NAVIDENT)), null))
				.isInstanceOf(MissingClaimException.class);
	}

	JwtToken createAzureClientCredentialGrantWithOptionalIdtypClaimToken() {
		return createAzureToken(defaultNavClaimSet(APP_CLAIM_SUB, APP_CLAIM_OID, APP_CLAIM_AZP, APP_CLAIM_AZP_NAME, USER_CLAIM_NAVIDENT)
				.claim(AzureAdFlowSporingHandler.OPTIONAL_CLAIM_SET_IDTYP, AzureAdFlowSporingHandler.OPTIONAL_CLAIM_SET_IDTYP_VALUE));
	}

	JwtToken createAzureClientCredentialGrantToken() {
		return createAzureToken(defaultNavClaimSet(APP_CLAIM_SUB, APP_CLAIM_OID, APP_CLAIM_AZP, APP_CLAIM_AZP_NAME, USER_CLAIM_NAVIDENT));
	}

	JwtToken createAzureOnBehalfOfToken() {
		return createAzureToken(defaultNavClaimSet(APP_CLAIM_SUB, USER_CLAIM_OID, APP_CLAIM_AZP, APP_CLAIM_AZP_NAME, USER_CLAIM_NAVIDENT)
				.claim(AzureAdFlowSporingHandler.PROFILE_SCOPE_CLAIM_NAME, USER_CLAIM_NAME));
	}

	private JwtToken createAzureOnBehalfOfTokenWithoutNavIdent() {
		return createAzureToken(defaultNavClaimSet(APP_CLAIM_SUB, USER_CLAIM_OID, APP_CLAIM_AZP, APP_CLAIM_AZP_NAME, null)
				.claim(AzureAdFlowSporingHandler.PROFILE_SCOPE_CLAIM_NAME, USER_CLAIM_NAME));
	}

}