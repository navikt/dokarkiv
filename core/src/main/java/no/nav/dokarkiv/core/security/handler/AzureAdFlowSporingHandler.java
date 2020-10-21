package no.nav.dokarkiv.core.security.handler;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import org.slf4j.MDC;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Handleren registrerer sporingsdata for arkivering og endring av metadata etter arkivering.
 * Gjelder for OAuth 2.0 flytene:
 * * Client credential grant flow - system til system. https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-client-creds-grant-flow
 * * On-behalf-of flow - fra system med brukerkontekst. https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow
 *
 * @author Joakim Bjørnstad, Jbit AS
 * @see no.nav.dokarkiv.core.security.SporingHandlerInterceptor
 */
public class AzureAdFlowSporingHandler {
	// https://docs.microsoft.com/en-us/azure/active-directory/develop/active-directory-optional-claims#v10-and-v20-optional-claims-set
	static final String OPTIONAL_CLAIM_SET_IDTYP = "idtyp";
	static final String OPTIONAL_CLAIM_SET_IDTYP_VALUE = "app";
	// https://docs.microsoft.com/en-us/azure/active-directory/develop/access-tokens#payload-claims
	static final String DEFAULT_CLAIM_AZP = "azp";
	static final String DEFAULT_CLAIM_OID = "oid";
	static final String DEFAULT_CLAIM_SUB = "sub";
	static final String PROFILE_SCOPE_CLAIM_NAME = "name";

	public void handle(JwtToken token) {
		// https://docs.microsoft.com/en-us/azure/active-directory/develop/access-tokens#user-and-application-tokens
		if (token.containsClaim(OPTIONAL_CLAIM_SET_IDTYP, OPTIONAL_CLAIM_SET_IDTYP_VALUE)) {
			handleClientCredentialGrantFlow(token);
		} else if (isClientCredentialToken(token)) {
			handleClientCredentialGrantFlow(token);
		} else if (isOnBehalfOfToken(token)) {
			handleOnBehalfOfFlow(token);
		} else {
			throw new MissingClaimException("Azure token mangler nødvendige claims for sporing." +
					" 'sub' og 'oid' claims må være inkludert i tokenet.");
		}
	}

	private boolean isClientCredentialToken(JwtToken token) {
		final JwtTokenClaims jwtTokenClaims = token.getJwtTokenClaims();
		return jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_SUB) != null &&
				jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_OID) != null &&
				jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_SUB).equals(jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_OID));
	}

	private boolean isOnBehalfOfToken(JwtToken token) {
		final JwtTokenClaims jwtTokenClaims = token.getJwtTokenClaims();
		return jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_SUB) != null &&
				jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_OID) != null &&
				!jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_SUB).equals(jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_OID));
	}

	private void handleClientCredentialGrantFlow(JwtToken token) {
		final String azpClaim = token.getJwtTokenClaims().getStringClaim(DEFAULT_CLAIM_AZP);
		MDC.put(MDCConstants.MDC_CONSUMER_ID, azpClaim);
		MDC.put(MDCConstants.MDC_USER_ID, azpClaim);
		MDC.put(MDCConstants.MDC_USER_NAME, azpClaim);
	}

	private void handleOnBehalfOfFlow(JwtToken token) {
		final String azpClaim = token.getJwtTokenClaims().getStringClaim(DEFAULT_CLAIM_AZP);
		final String oidClaim = token.getJwtTokenClaims().getStringClaim(DEFAULT_CLAIM_OID);
		MDC.put(MDCConstants.MDC_CONSUMER_ID, azpClaim);
		MDC.put(MDCConstants.MDC_USER_ID, oidClaim);
		final String nameClaim = token.getJwtTokenClaims().getStringClaim(PROFILE_SCOPE_CLAIM_NAME);
		if (isNotBlank(nameClaim)) {
			MDC.put(MDCConstants.MDC_USER_NAME, nameClaim);
		} else {
			throw new MissingClaimException("Azure on-behalf-of token har ikke 'name' claim til innlogget bruker. " +
					"For arkivering behøver man det for sporing. 'name' claim er tilgjengelig med scope 'profile' i Azure OAuth 2.0.");
		}
	}
}
