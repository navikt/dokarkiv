package no.nav.dokarkiv.arkivervariant;

import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT,
		classes = {CoreConfig.class, ArkiverVariantConfig.class, AbstractArkiverVariantIT.Config.class},
		properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles({"itest", "wiremock"})
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = 0)
public abstract class AbstractArkiverVariantIT extends AbstractRestIT {

	protected static final String URL_ARKIVERVARIANT = "/rest/admin/arkivervariant/";
	protected static final String AZP_NAME_JOARKADMIN = "dev-fss:teamdokumenthandtering:joarkadmin";
	protected static final String MS_USER_ID_WITH_GROUP_ACCESS = "a123c63a-9821-4637-a23d-b706e5b24809";
	protected static final String MS_USER_ID_WITHOUT_GROUP_ACCESS = "b999c63a-9821-4637-a23d-b706e5b24809";

	@Configuration
	@Profile("itest")
	public static class Config {
		@Bean
		AzureAdGraphService azureAdGraphService() {
			AzureAdGraphService azureAdGraphService = mock(AzureAdGraphService.class);

			when(azureAdGraphService.isUserMemberOfGroup(eq(MS_USER_ID_WITH_GROUP_ACCESS),  anyString())).thenReturn(true);
			when(azureAdGraphService.isUserMemberOfGroup(eq(MS_USER_ID_WITHOUT_GROUP_ACCESS), anyString())).thenReturn(false);

			return azureAdGraphService;
		}
	}

}