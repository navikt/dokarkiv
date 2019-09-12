package no.nav.dokarkiv.sak;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.NavHeaders;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.security.AuthenticationResult;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.dokarkiv.core.security.ldap.NavUser;
import no.nav.dokarkiv.sak.dto.SakJson;
import no.nav.dokarkiv.sak.repository.HentSakerRepository;
import no.nav.dokarkiv.sak.repository.SakSearchCriteria;
import no.nav.dokarkiv.sak.repository.SakTestData;
import no.nav.dokarkiv.sak.util.SAMLSupport;
import no.nav.freg.security.test.oidc.tools.TestToolsAutoConfig;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {CoreConfig.class, SakConfig.class, TestToolsAutoConfig.class, SakResourceTest.Config.class},
        properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("itest,wiremock,ldap,oidc,saml")
@AutoConfigureWireMock(port = 0)
public class SakResourceTest extends AbstractRestIT {

    private static final String SAKER_URL = "/rest/saker/";
    private static final String CORRELATION_ID = "junit";
    private static final String ***passord=gammelt_passord***";

    private static final String NO_ACCESS_PERSON_USER_ID = "Z111111";

    @Inject
    private HentSakerRepository hentSakerRepository;

    @Value("${javax.net.ssl.trustStore}")
    private String trustStore;
    @Value("${javax.net.ssl.trustStorePassword}")
    private String trustStorePassword;
    @Value("${javax.net.ssl.trustStorePassword}")
    private String privateKeyPassword;

    public static class Config {
        @Bean
        NavLdapService navLdapService() {
            NavLdapService mockNavLdapService = mock(NavLdapService.class);
            when(mockNavLdapService.findByUserId(PERSON_USER_ID)).thenReturn(NavUser.builder()
                    .memberOf(new HashSet<>(asList("0000-GA-joark-vedlikehold")))
                    .userId(PERSON_USER_ID)
                    .userExistsInLdap(true)
                    .build());
            when(mockNavLdapService.findByUserId(NO_ACCESS_PERSON_USER_ID)).thenReturn(NavUser.builder()
                    .memberOf(new HashSet<>(asList("0000-GA-NOTHING")))
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
            when(mockNavLdapService.authenticateLdapUser(PERSON_USER_ID, PASSWORD)).thenReturn(AuthenticationResult.builder()
                    .user("itest")
                    .consumerId("itest")
                    .isValid(true)
                    .build());
            return mockNavLdapService;
        }
    }

    @BeforeClass
    public static void setupTrustStoreProperties() {
        System.setProperty("javax.net.ssl.trustStore", "/keystore");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456789");
    }

    @AfterClass
    public static void cleanupTrustStoreProperties() {
        System.clearProperty("javax.net.ssl.trustStore");
        System.clearProperty("javax.net.ssl.trustStorePassword");
    }

    @Test
    public void henter_sak_for_gitt_id() throws IOException {
        abacPermit();

        final Sak opprettetSak = hentSakerRepository
                .lagre(
                    new SakTestData()
                        .aktoerId("123")
                        .build()
                );

        reinitTransaction();

        ResponseEntity responseEntity = restTemplate.exchange(
                SAKER_URL + "/{id}",
                HttpMethod.GET,
                new HttpEntity<>(createHeadersWithCorrelationId()),
                Object.class, opprettetSak.getSakId());

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getHeaders().getContentType().toString(), containsString("application/json"));
        verifyEqual((LinkedHashMap)responseEntity.getBody(), opprettetSak);
    }

    @Test
    public void gir_404_naar_sak_ikke_finnes_for_gitt_id() throws IOException {
        abacPermit();

        ResponseEntity responseEntity = restTemplate.exchange(
                SAKER_URL + "/{id}",
                HttpMethod.GET,
                new HttpEntity<>(createHeadersWithCorrelationId()),
                Object.class, 1L);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
        LinkedHashMap responseBody = (LinkedHashMap)responseEntity.getBody();
        assertThat(responseBody.get("feilmelding"), notNullValue());
    }

    @Test
    public void gir_404_naar_ressurs_ikke_finnes() throws IOException {
        abacPermit();

        ResponseEntity responseEntity = restTemplate.exchange(
                SAKER_URL + "/v1/finnesikke/{id}",
                HttpMethod.GET,
                new HttpEntity<>(createHeadersWithCorrelationId()),
                String.class, 1L);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    public void gir_405_naar_operasjon_ikke_tillatt() throws IOException {
        abacPermit();

        Sak sak = new SakTestData().build();

        ResponseEntity createdResponse = restTemplate.exchange(
                SAKER_URL + "/1",
                HttpMethod.POST,
                new HttpEntity<>(new SakJson(sak), createHeadersWithCorrelationId()),
                Object.class);

        assertThat(createdResponse.getStatusCode(), is(HttpStatus.METHOD_NOT_ALLOWED));
    }

    @Test
    public void oppretter_sak_for_aktoer() throws IOException {
        abacPermit();

        Sak sak = new SakTestData()
            .aktoerId("1")
            .build();

        LinkedHashMap opprettetSak = createAndRetrieveAtLocation(sak);
        assertThat(opprettetSak.get("id"), notNullValue());
        assertThat(opprettetSak.get("aktoerId"), is(sak.getAktoerId()));
        assertThat(opprettetSak.get("orgnr"), nullValue());
    }

    @Test
    public void gir_400_naar_hverken_aktoer_eller_organisasjon_er_utfylt_ved_opprettelse_av_sak() throws IOException {
        abacPermit();

        Sak sak = Sak.builder().applikasjon(random(3)).tema(random(3)).build();

        ResponseEntity createdResponse = executePost(sak);

        verify400(createdResponse);
    }

    @Test
    public void beskyttede_ressurser_tilgjengelige_naar_gyldig_authheader_for_oidc() throws IOException {
        abacPermit();

        HttpHeaders headersWithAuthOIDC = createHeadersWithCorrelationId();
        verifyBeskyttedeRessurserTilgjengelig(headersWithAuthOIDC);
    }

    @Test
    public void beskyttede_ressurser_tilgjengelige_naar_gyldig_authheader_for_saml() throws IOException {
        abacPermit();

        HttpHeaders headersWithAuthSaml = createHeadersWithAuthSaml();
        verifyBeskyttedeRessurserTilgjengelig(headersWithAuthSaml);
    }

    @Test
    public void beskyttede_ressurser_tilgjengelig_naar_gyldig_basic_auth_header() throws IOException {
        abacPermit();

        HttpHeaders headersWithAuthBasic = createHeadersWithAuthBasic();
        verifyBeskyttedeRessurserTilgjengelig(headersWithAuthBasic);
    }

    @Test
    public void oppretter_sak_for_organisasjon() throws IOException {
        abacPermit();

        Sak sak = new SakTestData()
            .orgnr(SakTestData.generateValidOrgnr())
            .build();
        LinkedHashMap opprettetSak = createAndRetrieveAtLocation(sak);
        assertThat(opprettetSak.get("id"), notNullValue());
        assertThat(opprettetSak.get("orgnr"), is(sak.getOrgnr()));
        assertThat(opprettetSak.get("aktoerId"), nullValue());
    }

    @Test
    public void gir_konflikt_og_oppretter_ikke_ny_sak_dersom_fagsak_finnes_fra_foer() throws IOException {
        abacPermit();

        Sak sak = new SakTestData()
            .aktoerId("123")
            .fagsakNr("321")
            .applikasjon("Gosys")
            .build();

        ResponseEntity firstResponse = executePost(sak);

        assertThat(firstResponse.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(firstResponse.getHeaders().getContentType().toString(), containsString("application/json"));

        ResponseEntity secondResponse = executePost(sak);

        assertThat(secondResponse.getStatusCode(), is(HttpStatus.CONFLICT));
        assertThat(secondResponse.getHeaders().getContentType().toString(), containsString("application/json"));

        assertThat(hentSakerRepository.finnSaker(SakSearchCriteria.builder().build()), hasSize(1));
    }

    @Test
    public void oppretter_generell_sak_uten_applikasjon_angitt() throws IOException {
        abacPermit();

        Sak sak = new SakTestData()
            .aktoerId("1")
            .applikasjon(null)
            .build();
        LinkedHashMap opprettetSak = createAndRetrieveAtLocation(sak);
        assertThat(opprettetSak.get("id"), notNullValue());
        assertThat(opprettetSak.get("aktoerId"), is(sak.getAktoerId()));
        assertThat(opprettetSak.get("orgnr"), nullValue());
    }

    @Test
    public void kan_opprette_sak_med_applikasjon_uten_aa_angi_fagsaknr() throws IOException {
        abacPermit();

        Sak sak = new SakTestData()
            .aktoerId("1")
            .applikasjon("FS22")
            .build();
        LinkedHashMap opprettetSak = createAndRetrieveAtLocation(sak);
        assertThat(opprettetSak.get("id"), notNullValue());
        assertThat(opprettetSak.get("aktoerId"), is(sak.getAktoerId()));
        assertThat(opprettetSak.get("orgnr"), nullValue());
    }

    @Test
    public void applikasjon_er_paakrevd_for_fagsak() throws IOException {
        abacPermit();

        Sak sak = new SakTestData()
            .aktoerId("1")
            .applikasjon(null)
            .fagsakNr("123")
            .build();
        ResponseEntity createdResponse = executePost(sak);
        verify400(createdResponse);
    }

    @Test
	public void soeker_opp_saker_for_aktoer_id() throws IOException {
        abacPermit();

        opprett100Tilfeldigesaker();
        String aktoerId = RandomStringUtils.randomNumeric(9);
        Sak sak1 = hentSakerRepository.lagre(new SakTestData().aktoerId(aktoerId).build());
		Sak sak2 = hentSakerRepository.lagre(new SakTestData().aktoerId(aktoerId)
				.opprettettidspunkt(LocalDateTime.now().plusMinutes(1))
				.build());

        reinitTransaction();

        String url = UriComponentsBuilder.fromUriString(SAKER_URL)
                .queryParam("aktoerId", aktoerId)
                .build().toUriString();

        ResponseEntity response = executeGetRequest(url);

		verifySearchResponseMatching(response, asList(sak2, sak1));
    }

    @Test
    public void soeker_opp_saker_for_tema() throws IOException {
        abacPermit();

        opprett100Tilfeldigesaker();
        String tema = RandomStringUtils.randomAlphabetic(4);
        Sak sak = hentSakerRepository.lagre(new SakTestData().tema(tema).build());

        reinitTransaction();

        String url = UriComponentsBuilder.fromUriString(SAKER_URL)
                .queryParam("tema", sak.getTema())
                .queryParam("aktoerId", sak.getAktoerId())
                .build().toUriString();

        ResponseEntity response = executeGetRequest(url);

        verifySearchResponseMatching(response, singletonList(sak));
    }

    @Test
    public void soeker_opp_saker_for_flere_tema() throws IOException {
        abacPermit();

        opprett100Tilfeldigesaker();
        String tema1 = RandomStringUtils.randomAlphabetic(4);
        String tema2 = RandomStringUtils.randomAlphabetic(4);
        Sak sak1 = hentSakerRepository.lagre(new SakTestData().tema(tema1).build());
		Sak sak2 = hentSakerRepository.lagre(new SakTestData().tema(tema2)
				.aktoerId(sak1.getAktoerId())
				.opprettettidspunkt(LocalDateTime.now().plusMinutes(1))
				.build());

        reinitTransaction();

        String url = UriComponentsBuilder.fromUriString(SAKER_URL)
                .queryParam("tema", sak1.getTema())
                .queryParam("tema", sak2.getTema())
                .queryParam("aktoerId", sak1.getAktoerId())
                .build().toUriString();

        ResponseEntity response = executeGetRequest(url);

		verifySearchResponseMatching(response, asList(sak2, sak1));
    }

    @Test
    public void soeker_opp_saker_for_fagsaknr() throws IOException {
        abacPermit();

        opprett100Tilfeldigesaker();
        String fagsaknr = RandomStringUtils.randomNumeric(9);
        Sak sak = hentSakerRepository.lagre(new SakTestData().
            applikasjon(RandomStringUtils.randomAlphabetic(3)).
            fagsakNr(fagsaknr).build());

        reinitTransaction();

        String url = UriComponentsBuilder.fromUriString(SAKER_URL)
                .queryParam("fagsakNr", sak.getFagsakNr())
                .build().toUriString();

        ResponseEntity response = executeGetRequest(url);

        verifySearchResponseMatching(response, singletonList(sak));
    }

    @Test
    public void soeker_opp_saker_for_orgnr() throws IOException {
        abacPermit();

        opprett100Tilfeldigesaker();
        String orgnr = "974652250";
        Sak sak = hentSakerRepository.lagre(new SakTestData().orgnr(orgnr).build());

        reinitTransaction();

        String url = UriComponentsBuilder.fromUriString(SAKER_URL)
                .queryParam("orgnr", sak.getOrgnr())
                .build().toUriString();

        ResponseEntity response = executeGetRequest(url);

        verifySearchResponseMatching(response, singletonList(sak));
    }

    @Test
    public void soeker_opp_saker_for_applikasjon() throws IOException {
        abacPermit();

        opprett100Tilfeldigesaker();
        String applikasjon = RandomStringUtils.randomAlphabetic(9);
        Sak sak = hentSakerRepository.lagre(new SakTestData().applikasjon(applikasjon).build());

        reinitTransaction();

        String url = UriComponentsBuilder.fromUriString(SAKER_URL)
                .queryParam("applikasjon", sak.getApplikasjon())
                .queryParam("aktoerId", sak.getAktoerId())
                .build().toUriString();

        ResponseEntity response = executeGetRequest(url);

        verifySearchResponseMatching(response, singletonList(sak));
    }

    @Test
    public void soeker_opp_saker_for_kombinasjon_av_kriterier() throws IOException {
        abacPermit();

        opprett100Tilfeldigesaker();
        String fagsaknr = RandomStringUtils.randomNumeric(9);
        String applikasjon = RandomStringUtils.randomAlphabetic(9);
        String orgnr = SakTestData.generateValidOrgnr();
        String tema = RandomStringUtils.randomAlphabetic(4);

        hentSakerRepository.lagre(new SakTestData()
            .applikasjon(applikasjon)
            .orgnr(orgnr)
            .tema(tema)
            .build());

        hentSakerRepository.lagre(new SakTestData()
            .fagsakNr(fagsaknr)
            .orgnr(orgnr)
            .tema(tema)
            .build());

        hentSakerRepository.lagre(new SakTestData()
            .fagsakNr(fagsaknr)
            .applikasjon(applikasjon)
            .tema(tema)
            .build());

        hentSakerRepository.lagre(new SakTestData()
            .fagsakNr(fagsaknr)
            .applikasjon(applikasjon)
            .orgnr(orgnr)
            .build());

        Sak enesteGyldigeTreff = hentSakerRepository.lagre(new SakTestData()
            .fagsakNr(fagsaknr)
            .applikasjon(applikasjon)
            .orgnr(orgnr)
            .tema(tema)
            .build());

        reinitTransaction();

        String url = UriComponentsBuilder.fromUriString(SAKER_URL)
                .queryParam("fagsakNr", enesteGyldigeTreff.getFagsakNr())
                .queryParam("applikasjon", enesteGyldigeTreff.getApplikasjon())
                .queryParam("orgnr", enesteGyldigeTreff.getOrgnr())
                .queryParam("tema", enesteGyldigeTreff.getTema())
                .build().toUriString();

        ResponseEntity response = executeGetRequest(url);

        verifySearchResponseMatching(response, singletonList(enesteGyldigeTreff));
    }

    @Test
    public void gir_400_naar_hverken_aktoer_orgnr_eller_faksaknr_er_angitt_i_soek() throws IOException {
        abacPermit();

        opprett100Tilfeldigesaker();

        ResponseEntity response = executeGetRequest(SAKER_URL);

        verify400(response);
    }

    protected void abacPermit() {
        stubFor(post(urlEqualTo("/abac"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("abac/abac-permit.json")));
    }

    protected void reinitTransaction() {
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }

    protected HttpHeaders createHeadersWithCorrelationId() throws IOException {
        HttpHeaders httpHeaders = createHeadersWithServiceUserToken();
        httpHeaders.add(NavHeaders.X_CORRELATION_ID, CORRELATION_ID);
        return httpHeaders;
    }

    protected HttpHeaders createHeadersWithAuthSaml() throws IOException {
        SAMLSupport samlSupport = new SAMLSupport(trustStore, trustStorePassword, privateKeyPassword);
        String samlToken = samlSupport.createNewToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, "Saml " + samlToken);
        headers.add(NavHeaders.NAV_CALL_ID, "itest");
        headers.add(NavHeaders.X_CORRELATION_ID, CORRELATION_ID);
        return headers;
    }

    protected HttpHeaders createHeadersWithAuthBasic() throws IOException {
        String unencoded = PERSON_USER_ID + ":" + PASSWORD;
        String encoded = Base64.getEncoder().encodeToString(unencoded.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        headers.add(NavHeaders.NAV_CALL_ID, "itest");
        headers.add(NavHeaders.X_CORRELATION_ID, CORRELATION_ID);
        return headers;
    }

    private ResponseEntity createSakAndTestReponse(final Sak sak) throws IOException {

        ResponseEntity responseEntity = restTemplate.exchange(
                SAKER_URL,
                HttpMethod.POST,
                new HttpEntity<>(new SakJson(sak), createHeadersWithCorrelationId()),
                Object.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.CREATED));

        return responseEntity;
    }

    private void verifySearchResponseMatching(ResponseEntity responseEntity, List<Sak> skalMatche) {
        List<LinkedHashMap> responseList = verifySearchResponse(responseEntity, skalMatche.size());
        for (int i = 0; i < skalMatche.size(); i++) {
            verifyEqual(responseList.get(i), skalMatche.get(i));
        }
    }

    private List<LinkedHashMap> verifySearchResponse(ResponseEntity responseEntity, int expectedSize) {
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getHeaders().getContentType().toString(), containsString("application/json"));
        List<LinkedHashMap> responseList = (List<LinkedHashMap>)responseEntity.getBody();
        assertThat(responseList.size(), is(expectedSize));
        return responseList;
    }

    private void verify400(ResponseEntity responseEntity) {
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getHeaders().getContentType().toString(), containsString("application/json"));
        LinkedHashMap responseBody = (LinkedHashMap)responseEntity.getBody();
        assertThat(responseBody.get("uuid"), notNullValue());
        assertThat(responseBody.get("feilmelding"), notNullValue());
    }

    private void verifyEqual(LinkedHashMap sakMap, Sak sak) {
        assertThat(sakMap.get("id").toString(), is(sak.getSakId().toString()));
        assertThat(sakMap.get("tema"), is(sak.getTema()));
        if (StringUtils.isNotBlank(sak.getAktoerId())) {
            assertThat(sakMap.get("aktoerId"), is(sak.getAktoerId()));
        } else if (StringUtils.isNotBlank(sak.getOrgnr())) {
            assertThat(sakMap.get("orgnr"), is(sak.getOrgnr()));
        }
        if (StringUtils.isNotBlank(sak.getFagsakNr())) {
            assertThat(sakMap.get("fagsakNr"), is(sak.getFagsakNr()));
        }
        if(StringUtils.isNotBlank(sak.getApplikasjon())) {
            assertThat(sakMap.get("applikasjon"), is(sak.getApplikasjon()));
        }
    }

    private LinkedHashMap createAndRetrieveAtLocation(final Sak sak) throws IOException {

        final ResponseEntity createdResponse = createSakAndTestReponse(sak);

        ResponseEntity getResponse = restTemplate.exchange(
                createdResponse.getHeaders().getLocation().getPath(),
                HttpMethod.GET,
                new HttpEntity<>(createHeadersWithCorrelationId()),
                Object.class);

        assertThat(getResponse.getStatusCode(), is(HttpStatus.OK));

        return (LinkedHashMap)getResponse.getBody();
    }

    private void verifyBeskyttedeRessurserTilgjengelig(HttpHeaders headers) throws IOException {
        Sak sak = new SakTestData().aktoerId("1").build();

        ResponseEntity createdResponse = restTemplate.exchange(
                SAKER_URL,
                HttpMethod.POST,
                new HttpEntity<>(new SakJson(sak), headers),
                Object.class);

        assertThat(createdResponse.getStatusCode(), is(HttpStatus.CREATED));

        String url = UriComponentsBuilder.fromUriString(SAKER_URL)
                .queryParam("aktoerId", sak.getAktoerId())
                .build().toUriString();
        ResponseEntity searchResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class);
        assertThat(searchResponse.getStatusCode(), is(HttpStatus.OK));

        Sak opprettetSak = hentSakerRepository.lagre(new SakTestData().aktoerId("123").build());

        reinitTransaction();

        ResponseEntity getResponse = restTemplate.exchange(
                SAKER_URL + "/{id}",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class, opprettetSak.getSakId());

        assertThat(getResponse.getStatusCode(), is(HttpStatus.OK));
    }

    public ResponseEntity executePost(Sak sak) throws IOException {
        ResponseEntity createdResponse = restTemplate.exchange(
                SAKER_URL,
                HttpMethod.POST,
                new HttpEntity<>(new SakJson(sak), createHeadersWithCorrelationId()),
                Object.class);

        return createdResponse;
    }

    protected ResponseEntity executeGetRequest(String url) throws IOException {
        ResponseEntity getResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(createHeadersWithCorrelationId()),
                Object.class);

        return getResponse;
    }

    protected void opprett100Tilfeldigesaker() {
        for (int i = 0; i < 50; i++) {
            hentSakerRepository.lagre(new SakTestData().aktoerId(randomNumeric(5)).build());
            hentSakerRepository.lagre(new SakTestData().orgnr(SakTestData.generateValidOrgnr()).build());
        }
    }

}
