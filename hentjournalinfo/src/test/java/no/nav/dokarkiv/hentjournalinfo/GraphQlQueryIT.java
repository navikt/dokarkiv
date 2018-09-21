package no.nav.dokarkiv.hentjournalinfo;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider.FIL_UUID;
import static no.nav.dokarkiv.core.security.JwtClaimsBuilderProvider.openAmClaimsBuilder;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestAssertUtils.assertBrukere;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestAssertUtils.assertDokumentInfo;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestAssertUtils.assertJournalpost;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestAssertUtils.assertKnyttetDokumentList;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestQueryUtils.createDokumentInfoRequest;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestQueryUtils.createFilRequest;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestQueryUtils.createJournalpostDokumentInfoRequest;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestQueryUtils.createJournalpostRequest;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.domain.builder.DokumentFilBuilder;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.security.abac.AuthorizationException;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.dokarkiv.hentjournalinfo.dto.DokumentInfo;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.ExceptionType;
import no.nav.dokarkiv.hentjournalinfo.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils;
import no.nav.freg.security.test.oidc.tools.OidcTestService;
import no.nav.freg.security.test.oidc.tools.TestToolsAutoConfig;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.data.ldap.AutoConfigureDataLdap;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Description;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {CoreConfig.class, HentJournalInfoConfig.class, TestToolsAutoConfig.class})
@ActiveProfiles("itest,wiremock,ldap,oidc")
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@AutoConfigureDataLdap
@AutoConfigureWireMock(port = 0)
@Transactional
public class GraphQlQueryIT {

    private static final byte[] FIL_CONTENT = "Test".getBytes();

    protected String OIDC_TOKEN_PERSON_USER_TEST;
    protected String OIDC_TOKEN_SERVICE_USER_TEST;
    protected final String SERVICE_USER_ID = "srvdokarkiv";
    protected final String PERSON_USER_ID = "Z990782";

    @Inject
    private JoarkRepository joarkRepository;
    @Inject
    private DokumentFilRepository dokumentFilRepository;
    @Inject
    private DokumentinfoRepository dokumentinfoRepository;
    @Inject
    private TestRestTemplate testRestTemplate;
    @Inject
    private OidcTestService oidcTestService;

    @Before
    public void setUp() {
        OIDC_TOKEN_PERSON_USER_TEST = "Bearer " + oidcTestService.createOidc(openAmClaimsBuilder().subject(PERSON_USER_ID)
                .build());
        OIDC_TOKEN_SERVICE_USER_TEST = "Bearer " + oidcTestService.createOidc(openAmClaimsBuilder().subject(SERVICE_USER_ID)
                .build());
        RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
                .userId("itestuser")
                .componentId("itest")
                .build());
    }

    @After
    public void deleteAll() {
        //Feiler
    }


    @Test
    @Description("Example request")
    public void shouldGetAll() throws Exception {
        abacPermit();
        String filUuid = FilDetaljer.generateUuid();
        Journalpost journalpost = joarkRepository.save(TestDataUtils.createJournalpostBuilder(filUuid).build());
        dokumentFilRepository.save(DokumentFilBuilder.getDokumentFilBuilder()
                .filUuid(filUuid)
                .fil(FIL_CONTENT)
                .opprettetKildeNavn("test")
                .build());
        TestTransaction.flagForCommit();
        TestTransaction.end();

        GraphQLRequest graphQLRequest = createJournalpostDokumentInfoRequest(journalpost.getJournalpostId(), journalpost.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId());

        GraphQlResponse response = testRestTemplate.postForObject("/rest/graphql", new HttpEntity<>(graphQLRequest, oidcHeaders()), GraphQlResponse.class);

        assertThat(response.getDataWrapper(), notNullValue());
        assertThat(response.getErrors(), nullValue());


        assertDokumentInfo(response.getDataWrapper().getDokumentInfo());
        assertJournalpost(response.getDataWrapper().getJournalpost());

    }

    @Test
    public void shouldGetJournalpostInfo() throws Exception {
        abacPermit();
        Journalpost journalpost = joarkRepository.save(TestDataUtils.createJournalpostBuilder(FIL_UUID).build());
        TestTransaction.flagForCommit();
        TestTransaction.end();

        HttpEntity request = new HttpEntity<>(createJournalpostRequest(journalpost.getJournalpostId()), oidcHeaders());

        GraphQlResponse response = testRestTemplate.postForObject("/rest/graphql", request, GraphQlResponse.class);

        assertThat(response.getDataWrapper(), notNullValue());
        assertThat(response.getErrors(), nullValue());

        no.nav.dokarkiv.hentjournalinfo.dto.Journalpost journalpostResponse = response.getDataWrapper().getJournalpost();
        assertJournalpost(journalpostResponse);
        assertBrukere(journalpostResponse.getBrukere());
        assertKnyttetDokumentList(journalpostResponse.getKnyttetDokumentList());

    }

    @Test
    public void shouldGetDokumentInfo() throws Exception {
        abacPermit();
        Journalpost journalpost = joarkRepository.save(TestDataUtils.createJournalpostBuilder(FIL_UUID).build());
        TestTransaction.flagForCommit();
        TestTransaction.end();

        HttpEntity request = new HttpEntity<>(createDokumentInfoRequest(journalpost.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId()), oidcHeaders());

        GraphQlResponse response = testRestTemplate.postForObject("/rest/graphql", request, GraphQlResponse.class);

        assertThat(response.getDataWrapper(), notNullValue());
        assertThat(response.getErrors(), nullValue());

        DokumentInfo dokumentInfo = response.getDataWrapper().getDokumentInfo();
        assertDokumentInfo(dokumentInfo);
    }

    @Test
    @Description("This functionality is not activated at this moment. This test validates that the query is disabled")
    public void shouldGetFil() throws Exception {
        abacPermit();
        Journalpost journalpost = joarkRepository.save(TestDataUtils.createJournalpostBuilder(FIL_UUID).build());
        dokumentFilRepository.save(DokumentFilBuilder.getDokumentFilBuilder()
                .filUuid(FIL_UUID)
                .fil(FIL_CONTENT)
                .opprettetKildeNavn("test")
                .build());
        TestTransaction.flagForCommit();
        TestTransaction.end();

        HttpEntity request = new HttpEntity<>(createFilRequest(journalpost.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId(), journalpost.getJournalpostId()), oidcHeaders());

        GraphQlResponse response = testRestTemplate.postForObject("/rest/graphql", request, GraphQlResponse.class);
        assertThat(response.getDataWrapper(), nullValue());
        assertThat(response.getErrors()
                .get(0)
                .getMessage(), containsString("Validation error of type FieldUndefined: Field 'dokumentFil' in type 'Query' is undefined @ 'dokumentFil'"));

//        String fil = response.getDataWrapper().getDokumentFil();
//        assertThat(response.getDataWrapper(), notNullValue());
//        assertThat(Base64.decode(fil), is(new String(FIL_CONTENT, StandardCharsets.UTF_8)));
    }

    @Test
    public void shouldReturnErrorIfJournalpostNotFound() throws Exception {
        abacPermit();

        JSONObject request = new JSONObject();
        request.put("query", "query {journalpost(journalpostId: 1) {tema}}");

        HttpEntity httpEntity = new HttpEntity(request.toString(), oidcHeaders());
        GraphQlResponse response = testRestTemplate.postForObject("/rest/graphql", httpEntity, GraphQlResponse.class);
        assertThat(response.getErrors()
                .get(0)
                .getMessage(), containsString("Journalpost ikke funnet. journalpostId=1"));

    }

    @Test
    public void shouldReturnDokumentNotFoundErrorWhenDokumentIsDeleted() throws Exception {
        abacPermit();
        Journalpost journalpost = TestDataUtils.createJournalpostBuilder(FIL_UUID).build();
        journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setSlettet(true);
        joarkRepository.save(journalpost);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        HttpEntity request = new HttpEntity<>(createDokumentInfoRequest(journalpost.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId()), oidcHeaders());

        GraphQlResponse response = testRestTemplate.postForObject("/rest/graphql", request, GraphQlResponse.class);
        assertThat(response.getErrors().size(), is(1));
        assertThat(response.getErrors().get(0).getMessage(), containsString("DokumentInfo ikke funnet. dokumentInfoId="));
        assertThat(response.getErrors().get(0).getException(), is(DokumentInfoIkkeFunnetException.class.getSimpleName()));
        assertThat(response.getErrors().get(0).getExceptionType(), is(ExceptionType.FUNCTIONAL));
    }

    @Test
    public void shouldReturnDokumentNotFoundError() throws Exception {
        abacPermit();

        HttpEntity request = new HttpEntity<>(createDokumentInfoRequest(123L), oidcHeaders());

        GraphQlResponse response = testRestTemplate.postForObject("/rest/graphql", request, GraphQlResponse.class);
        assertThat(response.getErrors().size(), is(1));
        assertThat(response.getErrors().get(0).getMessage(), containsString("DokumentInfo ikke funnet. dokumentInfoId=123"));
        assertThat(response.getErrors().get(0).getException(), is(DokumentInfoIkkeFunnetException.class.getSimpleName()));
        assertThat(response.getErrors().get(0).getExceptionType(), is(ExceptionType.FUNCTIONAL));
    }

    @Test
    public void shouldReturnJournalpostNotFoundError() throws Exception {
        abacPermit();

        HttpEntity request = new HttpEntity<>(createJournalpostRequest(123L), oidcHeaders());

        GraphQlResponse response = testRestTemplate.postForObject("/rest/graphql", request, GraphQlResponse.class);
        assertThat(response.getErrors().size(), is(1));
        assertThat(response.getErrors().get(0).getMessage(), containsString("Journalpost ikke funnet. journalpostId=1"));
        assertThat(response.getErrors().get(0).getException(), is(JournalpostIkkeFunnetException.class.getSimpleName()));
        assertThat(response.getErrors().get(0).getExceptionType(), is(ExceptionType.FUNCTIONAL));
    }

    @Test
    public void shouldReturnAuthorizationExceptionWhenAbacDenyForDokumentInfoQuery() throws Exception {
        abacDeny();
        Journalpost journalpost = TestDataUtils.createJournalpostBuilder(FIL_UUID).build();
        journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setSlettet(true);
        joarkRepository.save(journalpost);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        HttpEntity request = new HttpEntity<>(createDokumentInfoRequest(journalpost.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId()), oidcHeaders());

        GraphQlResponse response = testRestTemplate.postForObject("/rest/graphql", request, GraphQlResponse.class);

        assertThat(response.getErrors(), notNullValue());
        assertThat(response.getErrors().size(), is(1));
        assertThat(response.getErrors()
                .get(0)
                .getMessage(), containsString("Feilet ved henting av data (/dokumentInfo) : Bruker har ikke tilgang til journalpost"));
        assertThat(response.getErrors().get(0).getException(), is(AuthorizationException.class.getSimpleName()));
        assertThat(response.getErrors().get(0).getExceptionType(), is(ExceptionType.FUNCTIONAL));
    }

    @Test
    public void shouldReturnAuthorizationExceptionWhenAbacDenyForJournalpostQuery() throws Exception {
        abacDeny();
        Journalpost journalpost = TestDataUtils.createJournalpostBuilder(FIL_UUID).build();
        journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setSlettet(true);
        joarkRepository.save(journalpost);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        HttpEntity request = new HttpEntity<>(createJournalpostRequest(journalpost.getJournalpostId()), oidcHeaders());

        GraphQlResponse response = testRestTemplate.postForObject("/rest/graphql", request, GraphQlResponse.class);

        assertThat(response.getErrors(), notNullValue());
        assertThat(response.getErrors().size(), is(1));
        assertThat(response.getErrors()
                .get(0)
                .getMessage(), containsString("Feilet ved henting av data (/journalpost) : Bruker har ikke tilgang til journalpost"));
        assertThat(response.getErrors().get(0).getException(), is(AuthorizationException.class.getSimpleName()));
        assertThat(response.getErrors().get(0).getExceptionType(), is(ExceptionType.FUNCTIONAL));
    }

    protected void abacPermit() {
        stubFor(post(urlEqualTo("/abac"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("abac/abac-permit.json")));
    }


    protected void abacDeny() {
        stubFor(post(urlEqualTo("/abac"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("abac/abac-deny.json")));
    }

    protected HttpHeaders oidcHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);
        headers.add("Nav-Consumer-Token", OIDC_TOKEN_SERVICE_USER_TEST);
        return headers;
    }

}