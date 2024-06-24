package no.nav.dokarkiv.arkivervariant;

import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT,
		classes = {CoreConfig.class, ArkiverVariantConfig.class},
		properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles({"itest", "wiremock"})
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = 0)
public abstract class AbstractArkiverVariantIT extends AbstractRestIT {

	protected static final String URL_ARKIVERVARIANT = "/rest/admin/arkivervariant/";
	protected static final String AZP_NAME_JOARKADMIN = "dev-fss:teamdokumenthandtering:joarkadmin";
	protected static final String MS_USER_ID_WITH_GROUP_ACCESS = "a123c63a-9821-4637-a23d-b706e5b24809";
	protected static final String MS_USER_ID_WITHOUT_GROUP_ACCESS = "b999c63a-9821-4637-a23d-b706e5b24809";

	@BeforeEach
	public void setUp() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfJoarkVedlikehold(MS_ID_SAKSBEHANDLER);
	}
}