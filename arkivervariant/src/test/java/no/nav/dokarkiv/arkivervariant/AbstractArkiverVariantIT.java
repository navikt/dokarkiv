package no.nav.dokarkiv.arkivervariant;

import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.dokarkiv.core.security.ldap.NavUser;
import no.nav.freg.security.test.oidc.tools.TestToolsAutoConfig;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import wiremock.com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {CoreConfig.class, ArkiverVariantConfig.class, TestToolsAutoConfig.class,
				AbstractArkiverVariantIT.Config.class, TokenGeneratorConfiguration.class},
		properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles({"itest", "wiremock", "ldap", "oidc"})
@AutoConfigureWireMock(port = 0)
public abstract class AbstractArkiverVariantIT extends AbstractRestIT {

	protected static final String URL_ARKIVERVARIANT = "/rest/admin/arkivervariant/";
	protected static final String NO_ACCESS_PERSON_USER_ID = "Z111111";

	public static class Config {
		@Bean
		NavLdapService navLdapService() {
			NavLdapService mockNavLdapService = mock(NavLdapService.class);
			when(mockNavLdapService.findByUserId(PERSON_USER_ID)).thenReturn(NavUser.builder()
					.memberOf(new HashSet<>(Arrays.asList("0000-GA-joark-vedlikehold")))
					.userId(PERSON_USER_ID)
					.userExistsInLdap(true)
					.build());
			when(mockNavLdapService.findByUserId(NO_ACCESS_PERSON_USER_ID)).thenReturn(NavUser.builder()
					.memberOf(new HashSet<>(Arrays.asList("0000-GA-NOTHING")))
					.userId(NO_ACCESS_PERSON_USER_ID)
					.userExistsInLdap(true)
					.build());
			when(mockNavLdapService.findByServiceuserId(SERVICE_USER_ID)).thenReturn(NavUser.builder()
					.userId(SERVICE_USER_ID)
					.userExistsInLdap(true)
					.build());
			when(mockNavLdapService.findByServiceuserId(NO_ACCESS_SERVICE_USER_ID)).thenReturn(NavUser.builder()
					.userId(NO_ACCESS_SERVICE_USER_ID)
					.userExistsInLdap(true)
					.build());
			return mockNavLdapService;
		}

	}

	public static String classpathToString(String path) {
		return resourceUrlToString(Resources.getResource(path));
	}

	public static String resourceUrlToString(URL url) {
		try {
			return Resources.toString(url, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Could not convert url to String" + url);
		}
	}

	protected void abacPermit() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}
}
