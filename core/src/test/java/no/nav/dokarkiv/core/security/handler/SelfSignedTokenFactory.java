package no.nav.dokarkiv.core.security.handler;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import no.nav.security.token.support.core.jwt.JwtToken;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

final class SelfSignedTokenFactory {

	private static final RSAKey RSA_KEY;
	static {
		try {
			RSA_KEY = new RSAKeyGenerator(2048).generate();
		} catch (JOSEException e) {
			throw new IllegalStateException(e);
		}
	}

	static JwtToken createRestStsToken(JWTClaimsSet.Builder restStsClaimsBuilder) {
		return new JwtToken(createSignedJWT(restStsClaimsBuilder.build()).serialize());
	}

	static JwtToken createAzureToken(JWTClaimsSet.Builder azureClaimSetBuilder) {
		return new JwtToken(createSignedJWT(azureClaimSetBuilder.build()).serialize());
	}

	static JWTClaimsSet.Builder defaultNavClaimSet(String subject, String oid, String azp, String azpname, String navIdent) {
		return defaultAzureClaimSet(subject, oid, azp)
				.claim(AzureAdFlowSporingHandler.NAV_CUSTOM_CLAIM_NAVIDENT, navIdent)
				.claim(AzureAdFlowSporingHandler.NAV_CUSTOM_CLAIM_AZP_NAME, azpname);
	}

	static JWTClaimsSet.Builder defaultAzureClaimSet(String subject, String oid, String azp) {
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

	static JWTClaimsSet.Builder defaultRestStsClaimsSet(String subject) {
		Date now = new Date();
		return new JWTClaimsSet.Builder()
				.subject(subject)
				.claim("aud", Arrays.asList(subject, "preprod.local"))
				.claim("azp", subject)
				.claim("identType", "Systemressurs")
				.issuer("https://security-token-service.nais.preprod.local")
				.jwtID(UUID.randomUUID().toString())
				.notBeforeTime(now)
				.issueTime(now)
				.expirationTime(new Date(now.getTime() + TimeUnit.MINUTES.toMillis(60)));
	}

	private static SignedJWT createSignedJWT(JWTClaimsSet claimsSet) {
		try {
			JWSHeader.Builder header = new JWSHeader.Builder(JWSAlgorithm.RS256)
					.keyID(SelfSignedTokenFactory.RSA_KEY.getKeyID())
					.type(JOSEObjectType.JWT);

			SignedJWT signedJWT = new SignedJWT(header.build(), claimsSet);
			JWSSigner signer = new RSASSASigner(SelfSignedTokenFactory.RSA_KEY.toPrivateKey());
			signedJWT.sign(signer);

			return signedJWT;
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		}
	}
}
