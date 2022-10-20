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
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.dokarkiv.core.security.ldap.NavUser;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.test.JwkGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ExtendWith(MockitoExtension.class)
class NavSystemkontekstHandlerTest {

	private static final String APP_CLAIM_SUB = "srvskanmotreferanse";

	private static final String USER_NAVIDENT = "D999999";
	private static final String USER_NAME = "Donald Duck";

	private final NavLdapService navLdapServiceMock = mock(NavLdapService.class);
	private final AzureAdGraphService azureAdGraphService = mock(AzureAdGraphService.class);
	private final NavSystemkontekstHandler navSystemkontekstHandler = new NavSystemkontekstHandler(navLdapServiceMock, azureAdGraphService);

	@Test
	void shouldReturnFalseWhenHandledRestStsToken() throws IOException {
		boolean handle = navSystemkontekstHandler.handle(createRestStsToken(defaultRestStsClaimsSet(APP_CLAIM_SUB)), new MockHttpServletResponse(), null);
		assertThat(handle).isFalse();

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(APP_CLAIM_SUB);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_SUB);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(APP_CLAIM_SUB);
	}

	@Test
	void shouldReturnFalseWhenHandledRestStsTokenWithNavUserIdHeader() throws IOException {
		when(azureAdGraphService.hentFulltNavn(USER_NAVIDENT)).thenReturn(USER_NAME);
		boolean handle = navSystemkontekstHandler.handle(createRestStsToken(defaultRestStsClaimsSet(APP_CLAIM_SUB)), new MockHttpServletResponse(), USER_NAVIDENT);
		assertThat(handle).isFalse();

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(USER_NAVIDENT);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_SUB);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(USER_NAME);
	}

	@Test
	void shouldSetServiceuserContextWhenHandledRestStsTokenAndInvalidNavIdentFormat() throws IOException {
		when(azureAdGraphService.hentFulltNavn(USER_NAVIDENT)).thenReturn(USER_NAME);
		boolean handle = navSystemkontekstHandler.handle(createRestStsToken(defaultRestStsClaimsSet(APP_CLAIM_SUB)), new MockHttpServletResponse(), "DD99999");
		assertThat(handle).isFalse();

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(APP_CLAIM_SUB);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_SUB);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(APP_CLAIM_SUB);
	}

	@Test
	void shouldSetServiceuserContextWhenHandledRestStsTokenAndNavIdentNotFoundInLdap() throws IOException {
		when(azureAdGraphService.hentFulltNavn("Z111111")).thenReturn(null);
		boolean handle = navSystemkontekstHandler.handle(createRestStsToken(defaultRestStsClaimsSet(APP_CLAIM_SUB)), new MockHttpServletResponse(), "Z111111");
		assertThat(handle).isFalse();

		assertThat(MDC.get(MDCConstants.MDC_USER_ID)).isEqualTo(APP_CLAIM_SUB);
		assertThat(MDC.get(MDCConstants.MDC_CONSUMER_ID)).isEqualTo(APP_CLAIM_SUB);
		assertThat(MDC.get(MDCConstants.MDC_USER_NAME)).isEqualTo(APP_CLAIM_SUB);
	}

	@Test
	void shouldReturnTrueWhenHandledRestStsTokenNoServiceUser() throws IOException {
		MockHttpServletResponse response = new MockHttpServletResponse();
		boolean handle = navSystemkontekstHandler.handle(createRestStsToken(defaultRestStsClaimsSet(USER_NAVIDENT)), response, null);
		assertThat(handle).isTrue();
		assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
	}

	JwtToken createRestStsToken(JWTClaimsSet.Builder restStsClaimsBuilder) {
		return new JwtToken(createSignedJWT(JwkGenerator.getDefaultRSAKey(), restStsClaimsBuilder.build()).serialize());
	}

	JWTClaimsSet.Builder defaultRestStsClaimsSet(String subject) {
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