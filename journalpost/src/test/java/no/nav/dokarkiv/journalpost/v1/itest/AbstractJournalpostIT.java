package no.nav.dokarkiv.journalpost.v1.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.auth0.jwt.JWT;
import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.JournalpostConfig;
import no.nav.freg.security.test.oidc.tools.TestToolsAutoConfig;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import wiremock.com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {CoreConfig.class, JournalpostConfig.class, TestToolsAutoConfig.class})
@ActiveProfiles("itest,wiremock,ldap,oidc")
@AutoConfigureWireMock(port = 0)
public abstract class AbstractJournalpostIT extends AbstractRestIT {

    static final String URL_JOURNALPOST = "/rest/journalpostapi/v1/journalpost/";
    static final String URL_DOKUMENTINFO= "/rest/journalpostapi/v1/dokumentInfo/";
    static final String FERDIGSTILL = "/ferdigstill";
    static final String KOPIER_QUERY = "kopierJournalpost?kildeJournalpostId=";
    static final String FERDIGSTILL_QUERY = "?forsoekFerdigstill=true";

    protected String OIDC_TOKEN_PERSON_USER_TEST;
    protected String OIDC_TOKEN_SERVICE_USER_TEST;

    void abacPermit() {
        stubFor(post(urlEqualTo("/abac"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("abac/abac-permit.json")));
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

    protected Journalpost buildAndCommit(final JournalpostBuilder builder) {
        Journalpost journalpost = joarkRepository.save(builder.build());
        TestTransaction.flagForCommit();
        TestTransaction.end();
        return journalpost;
    }

    protected HttpHeaders oidcHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);
        headers.add(NAV_CONSUMER_TOKEN, OIDC_TOKEN_SERVICE_USER_TEST);
        return headers;
    }

    protected void abacDeny() {
        stubFor(post(urlEqualTo("/abac"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("abac/abac-deny.json")));
    }

    protected String stringFromClasspath(String resourcename) throws IOException {
        return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(resourcename));
    }

    protected String getOidcTokenBody(String oidcToken) {
        return JWT.decode(oidcToken).getPayload();
    }
}
