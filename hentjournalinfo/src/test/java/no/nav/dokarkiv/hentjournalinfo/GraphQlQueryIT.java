package no.nav.dokarkiv.hentjournalinfo;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static no.nav.dokarkiv.core.datautil.BrukerTestDataProvider.BRUKER_ID;
import static no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider.FIL_UUID;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.FERDIGSTILT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.security.JwtClaimsBuilderProvider.openAmClaimsBuilder;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.HOVEDDOKUMENT_TITTEL;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.TILLEGGSOPPLYSNING_KEY;
import static no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils.TILLEGGSOPPLYSNING_VAL;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.domain.builder.DokumentFilBuilder;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.dokarkiv.hentjournalinfo.dto.DokumentInfo;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostStatus;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostType;
import no.nav.dokarkiv.hentjournalinfo.util.GraphQLRequest;
import no.nav.dokarkiv.hentjournalinfo.utils.TestDataUtils;
import no.nav.freg.security.test.oidc.tools.OidcTestService;
import no.nav.freg.security.test.oidc.tools.TestToolsAutoConfig;
import org.apache.geronimo.mail.util.Base64;
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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {CoreConfig.class, HentJournalInfoConfig.class, TestToolsAutoConfig.class})
@ActiveProfiles("itest,wiremock,ldap")
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

        Map<String, Object> variables = new HashMap<>();

        variables.put("journalpostId", journalpost.getJournalpostId());
        variables.put("dokumentInfoId", journalpost.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId());
        variables.put("type", "PDF");
        GraphQLRequest graphQLRequest = GraphQLRequest.builder()
                .variables(variables)
                .query("query ($journalpostId: Long! $dokumentInfoId: Long! $type: String!) " +
                        "{" +

                        "journalpost(journalpostId: $journalpostId) " +
                        "{" +
                        "tema " +
                        "journalpostDokumentInfoRelasjoner{tilknyttetJournalpostSom}" +
                        "saksrelasjon{fagsystem} " +
                        "brukere{brukerType} " +
                        "tilleggsopplysninger{key value}" +
                        "kryssreferanser{referanseId}" +
                        "} " +

                        "dokumentInfo(dokumentInfoId: $dokumentInfoId) " +
                        "{" +
                        "tittel " +
                        "tilleggsopplysninger{key value} " +
                        "journalpostRelasjoner{tilknyttetJournalpostSom} " +
                        "fildetaljerListe{filtype} " +
                        "skannetInnholdListe{vedleggNr}" +
                        "} " +

                        "fil(dokumentInfoId: $dokumentInfoId filtype: $type)" +
                        "}")
                .build();

        String response = testRestTemplate.postForObject("/rest/graphql", new HttpEntity<>(graphQLRequest, oidcHeaders()), String.class);

        GraphQlResponse jsonObject = new Gson().fromJson(response, GraphQlResponse.class);

//        assertThat(jsonObject.getData().getFil(), is(new String(FIL_CONTENT, StandardCharsets.UTF_8)));
//        assertThat(jsonObject.getData().getDokumentInfo().getTittel(), is(HOVEDDOKUMENT_TITTEL));
//        assertThat(jsonObject.getData().getDokumentInfo().getFildetaljerListe().get(0).getFiltype(), is("PDF"));
//        assertThat(jsonObject.getData()
//                .getDokumentInfo()
//                .getJournalpostRelasjoner()
//                .get(0)
//                .getTilknyttetJournalpostSom(), is(TilknyttetJournalpostSomCode.HOVEDDOKUMENT.name()));
//        assertThat(jsonObject.getData().getDokumentInfo().getSkannetInnholdListe().get(0).getVedleggNr(), is("1"));
//        assertThat(jsonObject.getData().getJournalpost().getBrukere().get(0).getBrukerType(), is(BrukerTypeCode.PERSON.name()));
//        assertThat(jsonObject.getData().getJournalpost().getKryssreferanser().get(0).getReferanseId(), is(REFERANSEID));
//        assertThat(jsonObject.getData()
//                .getJournalpost()
//                .getJournalpostDokumentInfoRelasjoner()
//                .get(0)
//                .getTilknyttetJournalpostSom(), is(TilknyttetJournalpostSomCode.HOVEDDOKUMENT.name()));
//        assertThat(jsonObject.getData().getJournalpost().getSaksrelasjon().getFagsystem(), is(FagsystemCode.AO01.name()));
//        assertThat(jsonObject.getData()
//                .getJournalpost()
//                .getTilleggsopplysninger()
//                .get(0)
//                .getValue(), is(TILLEGGSOPPLYSNING_VAL));
//        assertThat(jsonObject.getData().getJournalpost().getTilleggsopplysninger().get(0).getKey(), is(TILLEGGSOPPLYSNING_KEY));


    }

    @Test
    public void shouldGetJournalpostInfo() throws Exception {
        abacPermit();
        Journalpost journalpost = joarkRepository.save(TestDataUtils.createJournalpostBuilder(FIL_UUID).build());
        TestTransaction.flagForCommit();
        TestTransaction.end();

        HttpEntity request = new HttpEntity<>(createJournalpostRequest(journalpost.getJournalpostId()), oidcHeaders());

        GraphQlResponse response = testRestTemplate.postForObject("/rest/graphql", request, GraphQlResponse.class);
        no.nav.dokarkiv.hentjournalinfo.dto.Journalpost journalpostResponse = response.getJournalpost();

        assertThat(journalpostResponse.getTema(), is(FagsystemCode.PEN.name()));
        assertThat(journalpostResponse.getJournalpostStatus(), is(JournalpostStatus.FERDIGSTILT_SENTRALPRINT));
        assertThat(journalpostResponse.getJournalpostType(), is(JournalpostType.UTGAAENDE));
        assertThat(journalpostResponse.getBrukere().get(0).getBrukerId(), is(BRUKER_ID));
        assertThat(journalpostResponse.getBrukere().get(0).getBrukerType(), is(BrukerTypeCode.PERSON.name()));
        assertThat(journalpostResponse.getKnyttetDokumentList().get(0).getTilknyttetJournalpostSom(), is(HOVEDDOKUMENT.name()));
        assertThat(journalpostResponse.getKnyttetDokumentList().get(0).getDokumentInfo().getTittel(), is(HOVEDDOKUMENT_TITTEL));
        assertThat(journalpostResponse.getKnyttetDokumentList().get(0).getDokumentInfo().getStatus(), is(FERDIGSTILT.name()));

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
        DokumentInfo dokumentInfo = response.getDokumentInfo();

        assertThat(dokumentInfo.getTittel(), is(HOVEDDOKUMENT_TITTEL));
        assertThat(dokumentInfo.getStatus(), is(FERDIGSTILT.name()));
        assertThat(dokumentInfo.getTilleggsopplysninger().get(TILLEGGSOPPLYSNING_KEY), is(TILLEGGSOPPLYSNING_VAL));
        assertThat(dokumentInfo.getKnyttetJournalpostList().get(0).getTilknyttetJournalpostSom(), is(HOVEDDOKUMENT.name()));
        assertThat(dokumentInfo.getKnyttetJournalpostList().get(0).getJournalpost().getTema(), is(FagsystemCode.PEN.name()));
        assertThat(dokumentInfo.getFilDetaljerList().get(0).getVariantFormat(), is(VariantFormatCode.ARKIV.name()));
        assertThat(dokumentInfo.getFilDetaljerList().get(0).getFiltype(), is("PDF"));
        assertThat(dokumentInfo.getOriginalJournalpost().getTema(), is(FagsystemCode.PEN.name()));
        assertThat(dokumentInfo.getOriginalJournalpost()
                .getJournalpostStatus(), is(JournalpostStatus.FERDIGSTILT_SENTRALPRINT));
        assertThat(dokumentInfo.getOriginalJournalpost().getJournalpostType(), is(JournalpostType.UTGAAENDE));
        assertThat(dokumentInfo.getOriginalJournalpost().getBrukere().get(0).getBrukerId(), is(BRUKER_ID));
        assertThat(dokumentInfo.getOriginalJournalpost().getBrukere().get(0).getBrukerType(), is(BrukerTypeCode.PERSON.name()));

    }

    @Test
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
        String fil = response.getDokumentFil();
        assertThat(Base64.decode(fil), is(new String(FIL_CONTENT, StandardCharsets.UTF_8)));
    }

    @Test
    public void shouldReturnErrorIfJournalpostNotFound() throws Exception {
        abacPermit();

        JSONObject request = new JSONObject();
        request.put("query", "query {journalpost(journalpostId: 1) {tema}}");

        HttpEntity httpEntity = new HttpEntity(request.toString(), oidcHeaders());
        String response = testRestTemplate.postForObject("/rest/graphql", httpEntity, String.class);
        JsonElement jsonElement = new Gson().fromJson(response, JsonObject.class).getAsJsonArray("errors").get(0);
        assertThat(jsonElement.getAsJsonObject()
                .get("message")
                .getAsString(), is("Journalpost ikke funnet. journalpostId=1"));

    }

    @Test
    public void shouldReturnErrorIfDokumentInfoNotFound() throws Exception {
        abacPermit();

        JSONObject request = new JSONObject();
        request.put("query", "query {dokumentInfo(dokumentInfoId: 1) {tittel}}");
        HttpEntity httpEntity = new HttpEntity(request.toString(), oidcHeaders());

        String response = testRestTemplate.postForObject("/rest/graphql", httpEntity, String.class);
        JsonElement jsonElement = new Gson().fromJson(response, JsonObject.class).getAsJsonArray("errors").get(0);
        assertThat(jsonElement.getAsJsonObject()
                .get("message")
                .getAsString(), is("Fant ingen dokumentInfo med id=1 i databasen"));

    }

    private GraphQLRequest createFilRequest(Long dokumentInfoId, Long journalpostId) {
        Map<String, Object> variables = new HashMap<>();

        variables.put("dokId", dokumentInfoId);
        variables.put("jpId", journalpostId);
        return GraphQLRequest.builder()
                .variables(variables)
                .query("query ($dokId: Long! $jpId: Long!) {  dokumentFil(dokumentInfoId:$dokId, journalpostId:$jpId )}")
                .build();
    }

    private GraphQLRequest createJournalpostRequest(Long journalpostId) {

        Map<String, Object> variables = new HashMap<>();

        variables.put("id", journalpostId);
        return GraphQLRequest.builder()
                .variables(variables)
                .query("query ($id: Long!) " +
                        "{" +
                        "journalpost(journalpostId: $id) {" +
                        "    journalpostId" +
                        "    journalpostStatus" +
                        "    journalpostType" +
                        "    tema" +
                        "    tittel" +
                        "    brukere {" +
                        "      brukerId" +
                        "      brukerType" +
                        "    }" +
                        "    knyttetDokumentList {" +
                        "      dokumentInfoId" +
                        "      journalpostId" +
                        "      tilknyttetJournalpostSom" +
                        "      dokumentInfo {" +
                        "        dokumentInfoId" +
                        "        status" +
                        "        tilleggsopplysninger" +
                        "        tittel" +
                        "      }" +
                        "    }" +
                        "    " +
                        "  }" +
                        "}")
                .build();

    }

    private GraphQLRequest createDokumentInfoRequest(Long dokumentInfoId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", dokumentInfoId);


        return GraphQLRequest.builder()
                .variables(variables)
                .query("query ($id: Long!) " +
                        " {" +
                        "dokumentInfo(dokumentInfoId: $id) {" +
                        "    dokumentInfoId" +
                        "    tittel" +
                        "    status" +
                        "   filDetaljerList {" +
                        "      fildetaljerId" +
                        "      filtype" +
                        "      variantFormat" +
                        "    }" +
                        "    tilleggsopplysninger" +
                        "    knyttetJournalpostList {" +
                        "      journalpost {" +
                        "        tema" +
                        "      }" +
                        "      journalpostId" +
                        "      tilknyttetJournalpostSom" +
                        "    }" +
                        "    originalJournalpost {" +
                        "      journalpostId" +
                        "      journalpostStatus" +
                        "      journalpostType" +
                        "      tema" +
                        "      brukere {" +
                        "        brukerId" +
                        "        brukerType" +
                        "      }" +
                        "    }" +
                        "  }" +
                        "}")
                .build();
    }

    protected void abacPermit() {
        stubFor(post(urlEqualTo("/abac"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("abac/abac-permit.json")));
    }

    protected HttpHeaders oidcHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);
        headers.add("Nav-Consumer-Token", OIDC_TOKEN_SERVICE_USER_TEST);
        return headers;
    }
}