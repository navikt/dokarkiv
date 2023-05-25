package no.nav.dokarkiv.arkivervariant;

import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {CoreConfig.class, ArkiverVariantConfig.class, AbstractArkiverVariantIT.Config.class},
		properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles({"itest", "wiremock"})
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = 0)
public abstract class AbstractArkiverVariantIT extends AbstractRestIT {

	protected static final String URL_ARKIVERVARIANT = "/rest/admin/arkivervariant/";
	protected static final String NO_ACCESS_PERSON_USER_ID = "Z111111";

	public static class Config {
		@Bean
		AzureAdGraphService azureAdGraphService() {
			AzureAdGraphService azureAdGraphService = mock(AzureAdGraphService.class);
			when(azureAdGraphService.hentFulltNavn(PERSON_USER_ID)).thenReturn(PERSON_USER_NAME);
			when(azureAdGraphService.userInGroup(PERSON_USER_ID, "0000-GA-joark-vedlikehold")).thenReturn(true);

			when(azureAdGraphService.hentFulltNavn(NO_ACCESS_PERSON_USER_ID)).thenReturn(NO_ACCESS_PERSON_USER_ID);
			when(azureAdGraphService.userInGroup(NO_ACCESS_PERSON_USER_ID, "0000-GA-joark-vedlikehold")).thenReturn(false);
			return azureAdGraphService;
		}

	}

}
