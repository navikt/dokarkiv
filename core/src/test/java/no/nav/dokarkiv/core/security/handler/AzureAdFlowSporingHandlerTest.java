package no.nav.dokarkiv.core.security.handler;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.test.JwkGenerator;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class AzureAdFlowSporingHandlerTest {
	private static final String APP_CLAIM_AZP = "a2fb96a7-5294-48ea-a1de-a30599f95eb4";
	private static final String APP_CLAIM_SUB = "a2fb96a7-5294-48ea-a1de-a30599f95eb4";
	private static final String APP_CLAIM_OID = "a2fb96a7-5294-48ea-a1de-a30599f95eb4";
	private static final String USER_CLAIM_OID = "52968c79-cd9c-4368-a871-8e2b07f4d8b9";
	private static final String USER_CLAIM_NAME = "Donald Duck";

	private final AzureAdFlowSporingHandler azureAdFlowSporingHandler = new AzureAdFlowSporingHandler();

	@Test
	void shouldHandleClientCredentialGrantFlowWhenAppTokenContainsIdTypClaim() {
		azureAdFlowSporingHandler.handle(createAzureClientCredentialGrantWithOptionalIdtypClaimToken());

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(APP_CLAIM_AZP);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_AZP);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(APP_CLAIM_AZP);
	}

	@Test
	void shouldHandleClientCredentialGrantFlowWhenAppTokenDoesNotContainIdTypClaim() {
		azureAdFlowSporingHandler.handle(createAzureClientCredentialGrantToken());

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(APP_CLAIM_AZP);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_AZP);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(APP_CLAIM_AZP);
	}

	@Test
	void shouldHandleOnBehalfOfFlow() {
		azureAdFlowSporingHandler.handle(createAzureOnBehalfOfToken());

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(USER_CLAIM_OID);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_AZP);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(USER_CLAIM_NAME);
	}

	@Test
	void shouldThrowMissingClaimExceptionWhenAzureOnBehalfTokenIsMissingNameClaim() {
		assertThatThrownBy(() -> azureAdFlowSporingHandler.handle(createAzureToken(defaultAzureClaimSet(APP_CLAIM_SUB, USER_CLAIM_OID, APP_CLAIM_AZP))))
				.isInstanceOf(MissingClaimException.class);
	}

	JwtToken createAzureClientCredentialGrantWithOptionalIdtypClaimToken() {
		return createAzureToken(defaultAzureClaimSet(APP_CLAIM_SUB, APP_CLAIM_OID, APP_CLAIM_AZP)
				.claim(AzureAdFlowSporingHandler.OPTIONAL_CLAIM_SET_IDTYP, AzureAdFlowSporingHandler.OPTIONAL_CLAIM_SET_IDTYP_VALUE));
	}

	JwtToken createAzureClientCredentialGrantToken() {
		return createAzureToken(defaultAzureClaimSet(APP_CLAIM_SUB, APP_CLAIM_OID, APP_CLAIM_AZP));
	}

	JwtToken createAzureOnBehalfOfToken() {
		return createAzureToken(defaultAzureClaimSet(APP_CLAIM_SUB, USER_CLAIM_OID, APP_CLAIM_AZP)
				.claim(AzureAdFlowSporingHandler.PROFILE_SCOPE_CLAIM_NAME, USER_CLAIM_NAME));
	}

	JwtToken createAzureToken(JWTClaimsSet.Builder azureClaimSetBuilder) {
		return new JwtToken(createSignedJWT(JwkGenerator.getDefaultRSAKey(), azureClaimSetBuilder.build()).serialize());
	}

	JWTClaimsSet.Builder defaultAzureClaimSet(String subject, String oid, String azp) {
		Date now = new Date();
		return new JWTClaimsSet.Builder()
				.subject(subject)
				.jwtID(UUID.randomUUID().toString())
				.claim(AzureAdFlowSporingHandler.DEFAULT_CLAIM_OID, oid)
				.claim(AzureAdFlowSporingHandler.DEFAULT_CLAIM_AZP, azp)
				.notBeforeTime(now)
				.issueTime(now)
				.expirationTime(new Date(now.getTime() + TimeUnit.MINUTES.toMillis(60)));
	}

	SignedJWT createSignedJWT(RSAKey rsaJwk, JWTClaimsSet claimsSet) {
		try {
			JWSHeader.Builder header = new JWSHeader.Builder(JWSAlgorithm.RS256)
					.keyID(rsaJwk.getKeyID())
					.type(JOSEObjectType.JWT);

			SignedJWT signedJWT = new SignedJWT(header.build(), claimsSet);
			JWSSigner signer = new RSASSASigner(rsaJwk.toPrivateKey());
			signedJWT.sign(signer);

			return signedJWT;
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		}
	}
}