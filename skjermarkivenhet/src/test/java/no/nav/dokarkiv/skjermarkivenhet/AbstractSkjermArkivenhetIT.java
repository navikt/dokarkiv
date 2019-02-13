package no.nav.dokarkiv.skjermarkivenhet;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.freg.security.test.oidc.tools.TestToolsAutoConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {CoreConfig.class, SkjermArkivenhetConfig.class, TestToolsAutoConfig.class})
@ActiveProfiles("itest,wiremock,ldap,oidc")
@AutoConfigureWireMock(port = 0)
public abstract class AbstractSkjermArkivenhetIT extends AbstractRestIT {

	protected static final String URL_SKJERMARKIVENHET = "/rest/skjermarkivenhet";

	protected SkjermArkivenhetRequest createSkjermarkivenhetRequest(SkjermingTypeCode skjermingType, ArkivenhetCode arkivenhet, Long journalpostId, Long dokumentInfoId, VariantFormatCode variantFormat) {
		return SkjermArkivenhetRequest.builder()
				.skjerming(skjermingType)
				.arkivenhet(arkivenhet)
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.variant(variantFormat)
				.build();
	}

	protected void abacPermit() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}
}
