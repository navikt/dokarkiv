package no.nav.dokarkiv.arkivervariant;

import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.EnableWireMock;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT,
		classes = {CoreConfig.class, ArkiverVariantConfig.class},
		properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles({"itest", "wiremock"})
@EnableMockOAuth2Server
@EnableWireMock
public abstract class AbstractArkiverVariantIT extends AbstractRestIT {

	protected static final String URL_ARKIVERVARIANT = "/rest/admin/arkivervariant";
	protected static final String AZP_NAME_JOARKADMIN = "dev-fss:teamdokumenthandtering:joarkadmin";
}
