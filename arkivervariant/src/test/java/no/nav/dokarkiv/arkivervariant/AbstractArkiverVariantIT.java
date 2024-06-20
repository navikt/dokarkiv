package no.nav.dokarkiv.arkivervariant;

import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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

	protected static void stubMsGraphMemberOfEgenAnsatt(String msUserId) {
		stubMsGraphMemberOf(msUserId, "nav/msgraph-memberof-egenansatt.json");
	}

	protected static void stubMsGraphMemberOfNotEgenAnsatt(String msUserId) {
		stubMsGraphMemberOf(msUserId, "nav/msgraph-memberof-not-egenansatt.json");
	}

	protected static void stubMsGraphMemberOf(String msUserId, String bodyFile) {
		stubFor(get("/msgraph/users/" + msUserId + "/memberOf")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile(bodyFile)));
	}
}