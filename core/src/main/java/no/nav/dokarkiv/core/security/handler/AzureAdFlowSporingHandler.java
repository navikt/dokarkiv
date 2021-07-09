package no.nav.dokarkiv.core.security.handler;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.dokarkiv.core.security.ldap.NavUser;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import org.slf4j.MDC;

import static no.nav.dokarkiv.core.security.handler.HandlerConstants.NAVIDENT_PATTERN;
import static no.nav.dokarkiv.core.security.handler.HandlerConstants.NAVIDENT_REGEX;
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
@Slf4j
public class AzureAdFlowSporingHandler {
	private static final String ERROR_MELDING_PREFIX = "Tjeneste kalt med Azure Client Credential Grant Flow token og Nav-User-Id header.";
	private static final String ERROR_MELDING_SUFFIX = "Konsument må informeres og bes om å rette dette.";
	// https://docs.microsoft.com/en-us/azure/active-directory/develop/active-directory-optional-claims#v10-and-v20-optional-claims-set
	static final String OPTIONAL_CLAIM_SET_IDTYP = "idtyp";
	static final String OPTIONAL_CLAIM_SET_IDTYP_VALUE = "app";
	// https://docs.microsoft.com/en-us/azure/active-directory/develop/access-tokens#payload-claims
	static final String DEFAULT_CLAIM_AZP = "azp";
	static final String DEFAULT_CLAIM_OID = "oid";
	static final String DEFAULT_CLAIM_SUB = "sub";
	static final String DEFAULT_CLAIM_NAVIDENT = "NAVident";
	static final String PROFILE_SCOPE_CLAIM_NAME = "name";

	private final NavLdapService navLdapService;

	public AzureAdFlowSporingHandler(NavLdapService navLdapService) {
		this.navLdapService = navLdapService;
	}

	public void handle(JwtToken token, String navUserIdHeader) {
		// https://docs.microsoft.com/en-us/azure/active-directory/develop/access-tokens#user-and-application-tokens
		if (token.containsClaim(OPTIONAL_CLAIM_SET_IDTYP, OPTIONAL_CLAIM_SET_IDTYP_VALUE)) {
			handleClientCredentialGrantFlow(token, navUserIdHeader);
		} else if (isClientCredentialToken(token)) {
			handleClientCredentialGrantFlow(token, navUserIdHeader);
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

	private void handleClientCredentialGrantFlow(JwtToken token, String navUserIdHeader) {
		final String azpClaim = token.getJwtTokenClaims().getStringClaim(DEFAULT_CLAIM_AZP);
		if (navUserIdHeader == null) {
			handleClientCredentialGrantFlowAppContext(azpClaim);
		} else {
			handleClientCredentialGrantFlowNavUserIdHeaderContext(azpClaim, navUserIdHeader.trim());
		}
	}

	private void handleClientCredentialGrantFlowNavUserIdHeaderContext(String azpClaim, String navUserIdHeader) {
		if (NAVIDENT_PATTERN.matcher(navUserIdHeader).matches()) {
			final NavUser navUser = navLdapService.findByUserId(navUserIdHeader);
			if (navUser.isUserExistsInLdap()) {
				MDC.put(MDCConstants.MDC_USER_ID, navUserIdHeader);
				MDC.put(MDCConstants.MDC_USER_NAME, navUser.getFullname());
				MDC.put(MDCConstants.MDC_CONSUMER_ID, azpClaim);
			} else {
				log.error(ERROR_MELDING_PREFIX + " Fant ikke NAVIdent={} i onprem Active Directory. " + ERROR_MELDING_SUFFIX, navUserIdHeader);
				handleClientCredentialGrantFlowAppContext(azpClaim);
			}
		} else {
			log.error(ERROR_MELDING_PREFIX + " Ugyldig format på NAVIdent={}. Må matche \"" + NAVIDENT_REGEX + "\". " + ERROR_MELDING_SUFFIX, navUserIdHeader);
			handleClientCredentialGrantFlowAppContext(azpClaim);
		}
	}

	private void handleClientCredentialGrantFlowAppContext(String azpClaim) {
		MDC.put(MDCConstants.MDC_CONSUMER_ID, azpClaim);
		MDC.put(MDCConstants.MDC_USER_ID, azpClaim);
		MDC.put(MDCConstants.MDC_USER_NAME, azpClaim);
	}

	private void handleOnBehalfOfFlow(JwtToken token) {
		final String azpClaim = token.getJwtTokenClaims().getStringClaim(DEFAULT_CLAIM_AZP);
		final String oidClaim = token.getJwtTokenClaims().getStringClaim(DEFAULT_CLAIM_OID);
		final String navIdentClaim = token.getJwtTokenClaims().getStringClaim(DEFAULT_CLAIM_NAVIDENT);
		MDC.put(MDCConstants.MDC_CONSUMER_ID, azpClaim);
		MDC.put(MDCConstants.MDC_USER_ID, navIdentClaim != null ? navIdentClaim : oidClaim);
		final String nameClaim = token.getJwtTokenClaims().getStringClaim(PROFILE_SCOPE_CLAIM_NAME);
		if (isNotBlank(nameClaim)) {
			MDC.put(MDCConstants.MDC_USER_NAME, nameClaim);
		} else {
			throw new MissingClaimException("Azure on-behalf-of token har ikke 'name' claim til innlogget bruker. " +
					"For arkivering behøver man det for sporing. 'name' claim er tilgjengelig med scope 'profile' i Azure OAuth 2.0.");
		}
	}
}
