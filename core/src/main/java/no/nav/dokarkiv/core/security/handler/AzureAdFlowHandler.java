package no.nav.dokarkiv.core.security.handler;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.slf4j.MDC;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Handleren registrerer sporingsdata for arkivering og endring av metadata etter arkivering.
 * Gjelder for OAuth 2.0 flytene:
 * * Client credential grant flow - system til system. https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-client-creds-grant-flow
 * * On-behalf-of flow - fra system med brukerkontekst. https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow
 *
 * @see no.nav.dokarkiv.core.security.SporingHandlerInterceptor
 * @author Joakim Bjørnstad, Jbit AS
 */
public class AzureAdFlowHandler {
    // https://docs.microsoft.com/en-us/azure/active-directory/develop/active-directory-optional-claims#v10-and-v20-optional-claims-set
    private static final String OPTIONAL_CLAIM_SET_IDTYP = "idtyp";
    private static final String OPTIONAL_CLAIM_SET_IDTYP_VALUE = "app";
    // https://docs.microsoft.com/en-us/azure/active-directory/develop/access-tokens#payload-claims
    private static final String DEFAULT_CLAIM_AZP = "azp";
    private static final String DEFAULT_CLAIM_OID = "oid";
    private static final String PROFILE_SCOPE_CLAIM_NAME = "name";

    public void handle(JwtToken token) {
        // https://docs.microsoft.com/en-us/azure/active-directory/develop/access-tokens#user-and-application-tokens
        if (token.containsClaim(OPTIONAL_CLAIM_SET_IDTYP, OPTIONAL_CLAIM_SET_IDTYP_VALUE)) {
            handleClientCredentialGrantFlow(token);
        } else {
            handleOnBehalfOfFlow(token);
        }
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
            throw new IllegalArgumentException("Azure on-behalf-of token har ikke 'name' claim til innlogget bruker. " +
                    "For arkivering behøver man det for sporing. 'name' claim er tilgjengelig med scope 'profile' i Azure OAuth 2.0.");
        }
    }
}
