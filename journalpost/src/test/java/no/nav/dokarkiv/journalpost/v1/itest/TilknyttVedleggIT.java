package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.*;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVedlegg;
import no.nav.dokarkiv.journalpost.v1.api.FeiledeDokumenter;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggResponse;
import no.nav.dokarkiv.journalpost.v1.services.AccessLookupJournalpost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.stream.Collectors.joining;
import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.*;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.*;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.*;
import static no.nav.dokarkiv.core.util.TestDataUtils.KANAL_REFERANSE_ID;
import static no.nav.dokarkiv.core.util.TestdataFactory.createFerdigstiltJournalpostWithHoveddokument;
import static no.nav.dokarkiv.core.util.TestdataFactory.createFildetaljerOgFil;
import static no.nav.dokarkiv.journalpost.v1.api.ArsakKode.*;
import static no.nav.dokarkiv.journalpost.v1.util.FunctionalMatcher.where;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createDokumentVedlegg;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createDokumentVedleggList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class TilknyttVedleggIT extends AbstractJournalpostIT {

    private static final String TILKNYTT_VEDLEGG_PATH = "/tilknyttVedlegg";
    private static final String UGYLDIG_JOURNALPOST = "12312312312";
    private static final String TILLEGGOPPLYSNINGER_KEY = "DOK_ORG_DOK_INFO_ID";

    @MockitoSpyBean
    private JournalpostRepositorySkjermet journalpostRepositorySkjermet;

    @MockitoSpyBean
    private AccessLookupJournalpost accessLookupJournalpost;

    @BeforeEach
    public void setUp() {
        super.setUp();
        stubMsGraphGetUser(NAV_USER_ID);
        stubAzure();
        doAnswer(InvocationOnMock::callRealMethod)
                .when(accessLookupJournalpost).checkDocumentsCanBeAccessedByActor(any(TilknyttVedleggRequest.class));
    }

    @Test
    public void shouldOnlyAttachAccessApprovedDocuments() {
        Journalpost targetJournalpost = createJournalpostArkiv(U);
        Journalpost allowedJournalpost = createJournalpostArkiv(U);
        allowedJournalpost.setJournalstatus(J);
        Journalpost deniedJournalpost = createJournalpostSladdet();
        allowedJournalpost.setFagomrade(FAR);
        deniedJournalpost.setFagomrade(KTA);

        Long targetJournalpostId = saveJournalpost(targetJournalpost).getJournalpostId();
        Long allowedJournalpostId = saveJournalpost(allowedJournalpost).getJournalpostId();
        Long deniedJournalpostId = saveJournalpost(deniedJournalpost).getJournalpostId();

        Long allowedDokumentInfoId = allowedJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();
        Long deniedDokumentInfoId = deniedJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

        String safResponse = classpathToString("__files/saf/saf_response.json")
                .replace("${allowedDokumentInfoId}", allowedDokumentInfoId.toString())
                .replace("${deniedDokumentInfoId}", deniedDokumentInfoId.toString());
        stubFor(post(urlEqualTo("/safgraphql")).willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(safResponse)));

        completeCurrentAndStartNewTransaction();
        long antallDokumentInfo = dokumentInfoTestRepository.count();
        long antallDokumentFiler = dokumentFilTestRepository.count();

        var request = createTilknyttVedleggRequest(List.of(
                createDokumentVedlegg(allowedJournalpostId, allowedDokumentInfoId.toString(), null),
                createDokumentVedlegg(deniedJournalpostId, deniedDokumentInfoId.toString(), null)));
        var requestHttpEntity = new HttpEntity<>(request, createHeadersWithUserAndServiceUserToken());
        var response = restTemplate.exchange(
                apiJournalpostPath(targetJournalpostId + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, TilknyttVedleggResponse.class);
        completeCurrentAndStartNewTransaction();

        assertThat(response.getStatusCode(), is(MULTI_STATUS));
        assertNotNull(response.getBody());
        assertThat(response.getBody().getFeiledeDokumenter())
                .singleElement().satisfies(feiletDokument -> {
                    assertEquals(deniedJournalpostId.toString(), feiletDokument.getKildeJournalpostId());
                    assertEquals(deniedDokumentInfoId.toString(), feiletDokument.getDokumentInfoId());
                    assertEquals(SIKKERHETSBEGRENSNING, feiletDokument.getArsakKode());
                });
        assertThat(journalpostTestRepository.findById(targetJournalpostId).orElseThrow().getJournalpostDokumentInfoRelasjoner())
                .extracting(relasjon -> relasjon.getDokumentInfo().getDokumentInfoId())
                .contains(allowedDokumentInfoId)
                .doesNotContain(deniedDokumentInfoId)
                .hasSize(2);
        assertThat(dokumentInfoTestRepository.count()).isEqualTo(antallDokumentInfo);
        assertThat(dokumentInfoTestRepository.count()).isEqualTo(3);
        assertThat(dokumentFilTestRepository.count()).isEqualTo(4);
    }

    @ParameterizedTest
    @EnumSource(value = VariantFormatCode.class, names = {"ARKIV", "SLADDET"})
    public void shouldNotDuplicateAttachmentsWhenRequestIsRepeated(VariantFormatCode variantFormat) {
        Journalpost targetJournalpost = createJournalpostArkiv(U);
        Journalpost sourceJournalpost = variantFormat == SLADDET ? createJournalpostSladdet() : createJournalpostArkiv(U);
        sourceJournalpost.setJournalstatus(J);
        Long targetJournalpostId = saveJournalpost(targetJournalpost).getJournalpostId();
        Long sourceJournalpostId = saveJournalpost(sourceJournalpost).getJournalpostId();
        Long dokumentInfoId = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();
        generateAndStubSafResponse(sourceJournalpost);
        completeCurrentAndStartNewTransaction();

        var request = createTilknyttVedleggRequest(List.of(createDokumentVedlegg(sourceJournalpostId, dokumentInfoId.toString(), null)));
        var requestHttpEntity = new HttpEntity<>(request, createHeadersWithUserAndServiceUserToken());
        var path = apiJournalpostPath(targetJournalpostId + TILKNYTT_VEDLEGG_PATH);
        var firstResponse = restTemplate.exchange(path, PUT, requestHttpEntity, TilknyttVedleggResponse.class);

        completeCurrentAndStartNewTransaction();
        assertThat(firstResponse.getStatusCode(), is(OK));
        assertNotNull(firstResponse.getBody());
        assertThat(firstResponse.getBody().getFeiledeDokumenter()).isEmpty();

        var firstDokumentInfoIds = journalpostTestRepository.findById(targetJournalpostId).orElseThrow()
                .getJournalpostDokumentInfoRelasjoner().stream()
                .map(relasjon -> relasjon.getDokumentInfo().getDokumentInfoId()).toList();
        long antallDokumentInfo = dokumentInfoTestRepository.count();
        long antallDokumentFiler = dokumentFilTestRepository.count();

        var secondResponse = restTemplate.exchange(path, PUT, requestHttpEntity, TilknyttVedleggResponse.class);
        completeCurrentAndStartNewTransaction();
        assertThat(secondResponse.getStatusCode(), is(OK));
        assertNotNull(secondResponse.getBody());
        assertThat(secondResponse.getBody().getFeiledeDokumenter()).isEmpty();
        assertThat(journalpostTestRepository.findById(targetJournalpostId).orElseThrow().getJournalpostDokumentInfoRelasjoner())
                .extracting(relasjon -> relasjon.getDokumentInfo().getDokumentInfoId())
                .containsExactlyInAnyOrderElementsOf(firstDokumentInfoIds)
                .hasSize(2);
        assertEquals(antallDokumentInfo, dokumentInfoTestRepository.count());
        assertEquals(antallDokumentFiler, dokumentFilTestRepository.count());
    }

    @ParameterizedTest
    @EnumSource(value = JournalpostTypeCode.class, names = {"U", "N"})
    public void shouldTilknytteArkivVedleggTilJournalpost(JournalpostTypeCode journalpostTypeCode) {
        Journalpost targetJournalpost = createJournalpostArkiv(journalpostTypeCode);
        Journalpost sourceJournalpost = createJournalpostArkiv(journalpostTypeCode);
        sourceJournalpost.setJournalstatus(J);
        Long targetJournalpostId = saveJournalpost(targetJournalpost).getJournalpostId();
        Long sourcejournalpostId = saveJournalpost(sourceJournalpost).getJournalpostId();

        generateAndStubSafResponse(sourceJournalpost);
        completeCurrentAndStartNewTransaction();

        Long dokumentInfoId = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

        List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
        dokumentVedleggList.add(DokumentVedlegg.builder()
                .kildeJournalpostId(sourcejournalpostId)
                .dokumentInfoId(dokumentInfoId.toString())
                .build());

        HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

        TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

        HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
        var responseEntity = restTemplate.exchange(
                apiJournalpostPath(targetJournalpostId + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, TilknyttVedleggResponse.class);

        completeCurrentAndStartNewTransaction();

        Journalpost journalpostTilknyttetVedlegg = journalpostTestRepository.findById(targetJournalpostId).get();
        DokumentInfo sourceDokumentInfo = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

        assertThat(responseEntity.getStatusCode(), is(OK));
        assertNotNull(responseEntity.getBody());
        assertThat(responseEntity.getBody().getFeiledeDokumenter()).isEmpty();

        Optional<DokumentInfo> dokumentInfoKopi = journalpostTilknyttetVedlegg.getJournalpostDokumentInfoRelasjoner()
                .stream()
                .map(JournalpostDokumentInfoRelasjon::getDokumentInfo)
                .filter(j -> j.getDokumentInfoId().equals(dokumentInfoId)).findAny();
        assertTrue(dokumentInfoKopi.isPresent());
        assertEquals(sourceDokumentInfo.getDokumentInfoId(), dokumentInfoKopi.get().getDokumentInfoId());
    }

    @Test
    public void shouldTilknytteFlereVedleggTilJournalpost() {
        Journalpost targetJournalpost = createJournalpostArkiv(U);
        Journalpost sourceJournalpost1 = createJournalpostSladdet();
        Journalpost sourceJournalpost2 = createJournalpostSladdet();
        Journalpost sourceJournalpost3 = createJournalpostArkiv(U);
        sourceJournalpost3.setJournalstatus(J);
        Long targetJournalpostId = saveJournalpost(targetJournalpost).getJournalpostId();
        Long sourceJournalpostId1 = saveJournalpost(sourceJournalpost1).getJournalpostId();
        Long sourceJournalpostId2 = saveJournalpost(sourceJournalpost2).getJournalpostId();
        Long sourceJournalpostId3 = saveJournalpost(sourceJournalpost3).getJournalpostId();

        generateAndStubSafResponse(sourceJournalpost1, sourceJournalpost2, sourceJournalpost3);
        completeCurrentAndStartNewTransaction();

        Long sourceDokumentInfoId1 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId();
        Long sourceDokumentInfoId2 = sourceJournalpost2.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId();
        Long sourceDokumentInfoId3 = sourceJournalpost3.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId();

        List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
        dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId1, sourceDokumentInfoId1.toString(), null));
        dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId2, sourceDokumentInfoId2.toString(), 2));
        dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId3, sourceDokumentInfoId3.toString(), 1));

        HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

        TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

        HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
                apiJournalpostPath(targetJournalpostId + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, TilknyttVedleggResponse.class);

        completeCurrentAndStartNewTransaction();

        assertThat(responseEntity.getStatusCode(), is(OK));

        //Assert 1 Sladdet
        Journalpost journalpostTilknyttetVedlegg1 = journalpostTestRepository.findById(targetJournalpostId).get();
        DokumentInfo sourceDokumentInfo1 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
        DokumentInfo dokumentInfoKopi1 = journalpostTilknyttetVedlegg1.getJournalpostDokumentInfoRelasjoner()
                .stream()
                .filter(j -> j.getDokumentInfo().getTilleggsopplysninger().containsKey(TILLEGGOPPLYSNINGER_KEY))
                .filter(d -> d.getDokumentInfo().getTilleggsopplysninger().containsValue(sourceDokumentInfoId1.toString()))
                .findAny()
                .get()
                .getDokumentInfo();
        FilDetaljer sourceFilDetaljer1 = sourceDokumentInfo1.findFilDetaljerByVariantFormat(SLADDET);
        FilDetaljer filDetaljerKopi1 = dokumentInfoKopi1.findFilDetaljerByVariantFormat(ARKIV);
        DokumentFil sourceDokumentFil1 = dokumentFilTestRepository.findByFilUuid(sourceFilDetaljer1.getFilUuid());
        DokumentFil dokumentFilKopi1 = dokumentFilTestRepository.findByFilUuid(filDetaljerKopi1.getFilUuid());

        assertRelasjon(journalpostTilknyttetVedlegg1.getJournalpostId(), dokumentInfoKopi1, null);
        assertDokumentInfo(sourceDokumentInfo1, dokumentInfoKopi1);
        assertFildetaljer(sourceFilDetaljer1, filDetaljerKopi1);
        assertDokumentFil(sourceDokumentFil1, dokumentFilKopi1);

        //Assert 2 sladdet
        Journalpost journalpostTilknyttetVedlegg2 = journalpostTestRepository.findById(targetJournalpostId).get();
        DokumentInfo sourceDokumentInfo2 = sourceJournalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
        DokumentInfo dokumentInfoKopi2 = journalpostTilknyttetVedlegg2.getJournalpostDokumentInfoRelasjoner()
                .stream()
                .filter(j -> j.getDokumentInfo().getTilleggsopplysninger().containsKey(TILLEGGOPPLYSNINGER_KEY))
                .filter(d -> d.getDokumentInfo().getTilleggsopplysninger().containsValue(sourceDokumentInfoId2.toString()))
                .findAny()
                .get()
                .getDokumentInfo();
        FilDetaljer sourceFilDetaljer2 = sourceDokumentInfo2.findFilDetaljerByVariantFormat(SLADDET);
        FilDetaljer filDetaljerKopi2 = dokumentInfoKopi2.findFilDetaljerByVariantFormat(ARKIV);
        DokumentFil sourceDokumentFil2 = dokumentFilTestRepository.findByFilUuid(sourceFilDetaljer2.getFilUuid());
        DokumentFil dokumentFilKopi2 = dokumentFilTestRepository.findByFilUuid(filDetaljerKopi2.getFilUuid());

        assertRelasjon(journalpostTilknyttetVedlegg2.getJournalpostId(), dokumentInfoKopi2, 2);
        assertDokumentInfo(sourceDokumentInfo2, dokumentInfoKopi2);
        assertFildetaljer(sourceFilDetaljer2, filDetaljerKopi2);
        assertDokumentFil(sourceDokumentFil2, dokumentFilKopi2);


        //Assert 3 Arkiv
        Journalpost journalpostTilknyttetVedlegg3 = journalpostTestRepository.findById(targetJournalpostId).get();
        DokumentInfo sourceDokumentInfo3 = sourceJournalpost3.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
        DokumentInfo dokumentInfoKopi3 = journalpostTilknyttetVedlegg3.getJournalpostDokumentInfoRelasjoner()
                .stream()
                .filter(j -> j.getDokumentInfo().getDokumentInfoId().equals(sourceDokumentInfoId3))
                .findAny()
                .get()
                .getDokumentInfo();
        assertRelasjon(journalpostTilknyttetVedlegg3.getJournalpostId(), dokumentInfoKopi3, 1);
        assertThat(responseEntity.getStatusCode(), is(OK));
        assertEquals(sourceDokumentInfo3.getDokumentInfoId(), dokumentInfoKopi3.getDokumentInfoId());
    }

    @Test
    public void shouldTilknytte2av3VedleggTilJournalpost() {
        Journalpost journalpostVedlegg = createJournalpostArkiv(U);
        Journalpost sourceJournalpost1 = createJournalpostSladdet();
        Journalpost sourceJournalpost2 = createJournalpostSladdet();
        Journalpost sourceJournalpost3 = createJournalpostArkiv(U);
        Long journalpostIdVedlegg = saveJournalpost(journalpostVedlegg).getJournalpostId();
        Long sourceJournalpostId1 = saveJournalpost(sourceJournalpost1).getJournalpostId();
        Long sourceJournalpostId2 = saveJournalpost(sourceJournalpost2).getJournalpostId();
        Long sourceJournalpostId3 = saveJournalpost(sourceJournalpost3).getJournalpostId();

        generateAndStubSafResponse(sourceJournalpost1, sourceJournalpost2, sourceJournalpost3);
        completeCurrentAndStartNewTransaction();

        Long sourceDokumentInfoId1 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId();
        Long sourceDokumentInfoId2 = sourceJournalpost2.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId();
        Long sourceDokumentInfoId3 = sourceJournalpost3.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId();

        List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();

        dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId1, sourceDokumentInfoId1.toString()));
        dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId2, sourceDokumentInfoId2.toString()));
        dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId3, sourceDokumentInfoId3.toString()));

        HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

        TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

        HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
                apiJournalpostPath(journalpostIdVedlegg + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, TilknyttVedleggResponse.class);

        completeCurrentAndStartNewTransaction();

        assertThat(responseEntity.getStatusCode(), is(MULTI_STATUS));
        assertThat(responseEntity.getBody().getFeiledeDokumenter().get(0).getArsakKode(), is(UGYLDIG_STATUS));

        //Assert 1 Sladdet
        Journalpost journalpostTilknyttetVedlegg1 = journalpostTestRepository.findById(journalpostIdVedlegg).get();
        DokumentInfo sourceDokumentInfo1 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
        DokumentInfo dokumentInfoKopi1 = journalpostTilknyttetVedlegg1.getJournalpostDokumentInfoRelasjoner()
                .stream()
                .filter(j -> j.getDokumentInfo().getTilleggsopplysninger().containsKey(TILLEGGOPPLYSNINGER_KEY))
                .filter(d -> d.getDokumentInfo().getTilleggsopplysninger().containsValue(sourceDokumentInfoId1.toString()))
                .findAny()
                .get()
                .getDokumentInfo();
        FilDetaljer sourceFilDetaljer1 = sourceDokumentInfo1.findFilDetaljerByVariantFormat(SLADDET);
        FilDetaljer filDetaljerKopi1 = dokumentInfoKopi1.findFilDetaljerByVariantFormat(ARKIV);
        DokumentFil sourceDokumentFil1 = dokumentFilTestRepository.findByFilUuid(sourceFilDetaljer1.getFilUuid());
        DokumentFil dokumentFilKopi1 = dokumentFilTestRepository.findByFilUuid(filDetaljerKopi1.getFilUuid());

        assertRelasjon(journalpostTilknyttetVedlegg1.getJournalpostId(), dokumentInfoKopi1);
        assertDokumentInfo(sourceDokumentInfo1, dokumentInfoKopi1);
        assertFildetaljer(sourceFilDetaljer1, filDetaljerKopi1);
        assertDokumentFil(sourceDokumentFil1, dokumentFilKopi1);

        //Assert 2 sladdet
        Journalpost journalpostTilknyttetVedlegg2 = journalpostTestRepository.findById(journalpostIdVedlegg).get();
        DokumentInfo sourceDokumentInfo2 = sourceJournalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
        DokumentInfo dokumentInfoKopi2 = journalpostTilknyttetVedlegg2.getJournalpostDokumentInfoRelasjoner()
                .stream()
                .filter(j -> j.getDokumentInfo().getTilleggsopplysninger().containsKey(TILLEGGOPPLYSNINGER_KEY))
                .filter(d -> d.getDokumentInfo().getTilleggsopplysninger().containsValue(sourceDokumentInfoId2.toString()))
                .findAny()
                .get()
                .getDokumentInfo();
        FilDetaljer sourceFilDetaljer2 = sourceDokumentInfo2.findFilDetaljerByVariantFormat(SLADDET);
        FilDetaljer filDetaljerKopi2 = dokumentInfoKopi2.findFilDetaljerByVariantFormat(ARKIV);
        DokumentFil sourceDokumentFil2 = dokumentFilTestRepository.findByFilUuid(sourceFilDetaljer2.getFilUuid());
        DokumentFil dokumentFilKopi2 = dokumentFilTestRepository.findByFilUuid(filDetaljerKopi2.getFilUuid());

        assertRelasjon(journalpostTilknyttetVedlegg2.getJournalpostId(), dokumentInfoKopi2);
        assertDokumentInfo(sourceDokumentInfo2, dokumentInfoKopi2);
        assertFildetaljer(sourceFilDetaljer2, filDetaljerKopi2);
        assertDokumentFil(sourceDokumentFil2, dokumentFilKopi2);

        //Assert 3 Arkiv
        Journalpost journalpostTilknyttetVedlegg = journalpostTestRepository.findById(journalpostIdVedlegg).get();
        assertThat(journalpostTilknyttetVedlegg.getJournalpostDokumentInfoRelasjoner()
                .stream()
                .anyMatch(j -> j.getDokumentInfo().getDokumentInfoId().equals(sourceDokumentInfoId3)), is(false));
    }

    @Test
    public void shouldOnlyPerformAccessLookupForFagomraadeFARAndKTA() {
        Journalpost journalpostVedlegg = createJournalpostArkiv(U);
        Journalpost sourceJournalpost1 = createJournalpostArkiv(U);
        Journalpost sourceJournalpost2 = createJournalpostArkiv(U);
        Journalpost sourceJournalpost3 = createJournalpostArkiv(U);

        sourceJournalpost1.setFagomrade(PEN);
        sourceJournalpost2.setFagomrade(FAR);
        sourceJournalpost3.setFagomrade(KTA);

        Long journalpostIdVedlegg = saveJournalpost(journalpostVedlegg).getJournalpostId();
        Long sourceJournalpostId1 = saveJournalpost(sourceJournalpost1).getJournalpostId();
        Long sourceJournalpostId2 = saveJournalpost(sourceJournalpost2).getJournalpostId();
        Long sourceJournalpostId3 = saveJournalpost(sourceJournalpost3).getJournalpostId();

        generateAndStubSafResponse(sourceJournalpost1, sourceJournalpost2, sourceJournalpost3);
        completeCurrentAndStartNewTransaction();

        Long sourceDokumentInfoId1 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId();
        Long sourceDokumentInfoId2 = sourceJournalpost2.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId();
        Long sourceDokumentInfoId3 = sourceJournalpost3.findHoveddokumentDokumentInfoRelasjon()
                .getDokumentInfo()
                .getDokumentInfoId();

        List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();

        dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId1, sourceDokumentInfoId1.toString()));
        dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId2, sourceDokumentInfoId2.toString()));
        dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId3, sourceDokumentInfoId3.toString()));

        HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

        TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

        HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
        restTemplate.exchange(apiJournalpostPath(journalpostIdVedlegg + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, TilknyttVedleggResponse.class);

        completeCurrentAndStartNewTransaction();

        verify(2, postRequestedFor(urlEqualTo("/safgraphql")));
    }

    @Test
    public void shouldReturnForbiddenForWrongConsumer() {
        Journalpost journalpostVedlegg = createJournalpostArkiv(U);
        Journalpost sourceJournalpost = createJournalpostSladdet();
        Long journalpostIdVedlegg = journalpostTestRepository.persist(journalpostVedlegg).getJournalpostId();
        Long sourceJournalpostId = journalpostTestRepository.persist(sourceJournalpost).getJournalpostId();

        generateAndStubSafResponse(sourceJournalpost);
        completeCurrentAndStartNewTransaction();

        Long dokumentInfoId = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

        HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

        TilknyttVedleggRequest request = createTilknyttVedleggRequest(createDokumentVedleggList(sourceJournalpostId, dokumentInfoId
                .toString()));

        HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
        var responseEntity = restTemplate.exchange(
                apiJournalpostPath(journalpostIdVedlegg + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, String.class);
        assertThat(responseEntity.getStatusCode(), is(MULTI_STATUS));
    }

    @Test
    public void shouldReturnNotFoundForJournalpost() {
        generateAndStubSafResponse();

        List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();

        HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

        TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

        HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                apiJournalpostPath(UGYLDIG_JOURNALPOST + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, String.class);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    public void shouldReturnConflictForJournalpostWrongStatus() {
        Journalpost sourceJournalpost = createJournalpostSladdet();
        sourceJournalpost.setJournalstatus(M);
        Long journalpostIdVedlegg = journalpostTestRepository.persist(sourceJournalpost).getJournalpostId();
        Long sourceJournalpostId = journalpostTestRepository.persist(sourceJournalpost).getJournalpostId();

        generateAndStubSafResponse(sourceJournalpost);
        completeCurrentAndStartNewTransaction();

        Long dokumentInfoId = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

        HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

        TilknyttVedleggRequest request = createTilknyttVedleggRequest(createDokumentVedleggList(sourceJournalpostId, dokumentInfoId
                .toString()));

        var requestHttpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                apiJournalpostPath(journalpostIdVedlegg + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, String.class);

        assertThat(responseEntity.getStatusCode(), is(CONFLICT));
    }

    @Test
    public void shouldReturnFeiletDokumentListeAarsakKodeUgyldigStatus() {
        Journalpost journalpostVedlegg = createJournalpostArkiv(U);
        Journalpost sourceJournalpost = createJournalpostSladdet();
        sourceJournalpost.setJournalstatus(M);
        Long journalpostIdVedlegg = journalpostTestRepository.persist(journalpostVedlegg).getJournalpostId();
        Long sourceJournalpostId = journalpostTestRepository.persist(sourceJournalpost).getJournalpostId();

        generateAndStubSafResponse(sourceJournalpost);
        completeCurrentAndStartNewTransaction();

        Long dokumentInfoId = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

        HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

        TilknyttVedleggRequest request = createTilknyttVedleggRequest(createDokumentVedleggList(sourceJournalpostId, dokumentInfoId
                .toString()));

        var requestHttpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
                apiJournalpostPath(journalpostIdVedlegg + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, TilknyttVedleggResponse.class);


        assertThat(responseEntity.getStatusCode(), is(MULTI_STATUS));
        assertThat(responseEntity.getBody().getFeiledeDokumenter(), hasItem(allOf(
                where(dok -> Long.parseLong(((FeiledeDokumenter) dok).getDokumentInfoId()), is(dokumentInfoId)),
                where(FeiledeDokumenter::getArsakKode, is(UGYLDIG_STATUS))
        )));
    }

    @Test
    public void shouldReturnFeiletDokumentListeAarsakKodeIkkeFunnet() {
        Journalpost journalpostVedlegg = createJournalpostArkiv(U);
        Journalpost sourceJournalpost = createJournalpostSladdet();
        Long journalpostIdVedlegg = journalpostTestRepository.persist(journalpostVedlegg).getJournalpostId();
        Long sourceJournalpostId = journalpostTestRepository.persist(sourceJournalpost).getJournalpostId();

        generateAndStubSafResponse(sourceJournalpost);
        completeCurrentAndStartNewTransaction();

        HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

        TilknyttVedleggRequest request = createTilknyttVedleggRequest(createDokumentVedleggList(sourceJournalpostId, "200000345"));

        var requestHttpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
                apiJournalpostPath(journalpostIdVedlegg + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, TilknyttVedleggResponse.class);

        assertThat(responseEntity.getStatusCode(), is(MULTI_STATUS));
        assertThat(responseEntity.getBody().getFeiledeDokumenter().get(0).getArsakKode(), is(IKKE_FUNNET));
    }

    @Test
    void shouldReturnFeiletDokumentListeArsakKodeIkkeFunnetWhenDokumentFilDoesNotExist() {
        Journalpost targetJournalpost = createJournalpostArkiv(U);
        Journalpost sourceJournalpost = createJournalpostArkiv(U);
        sourceJournalpost.setJournalstatus(J);
        Journalpost sourceJournalpostSladdet = createJournalpostSladdet();
        sourceJournalpostSladdet.setJournalstatus(J);
        Long targetJournalpostId = saveJournalpost(targetJournalpost).getJournalpostId();
        Long sourcejournalpostId = saveJournalpost(sourceJournalpost).getJournalpostId();
        Long sourceJournalpostIdSladdet = saveJournalpost(sourceJournalpostSladdet).getJournalpostId();

        generateAndStubSafResponse(sourceJournalpost, sourceJournalpostSladdet);
        completeCurrentAndStartNewTransaction();

        JournalpostDokumentInfoRelasjon hoveddokumentDokumentInfoRelasjon = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon();
        JournalpostDokumentInfoRelasjon hoveddokumentDokumentInfoRelasjonSladdet = sourceJournalpostSladdet.findHoveddokumentDokumentInfoRelasjon();
        dokumentFilTestRepository.deleteByFilUuid(hoveddokumentDokumentInfoRelasjon.getDokumentInfo().getFildetaljerListe().stream().findFirst().get().getFilUuid());
        dokumentFilTestRepository.deleteByFilUuid(hoveddokumentDokumentInfoRelasjonSladdet.getDokumentInfo().getFildetaljerListe().stream().filter(f -> SLADDET == f.getVariantFormat()).findFirst().get().getFilUuid());
        completeCurrentAndStartNewTransaction();

        Long dokumentInfoId = hoveddokumentDokumentInfoRelasjon.getDokumentInfo().getDokumentInfoId();
        Long dokumentInfoIdSladdet = hoveddokumentDokumentInfoRelasjonSladdet.getDokumentInfo().getDokumentInfoId();
        List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
        dokumentVedleggList.add(DokumentVedlegg.builder()
                .kildeJournalpostId(sourcejournalpostId)
                .dokumentInfoId(dokumentInfoId.toString())
                .build());
        dokumentVedleggList.add(DokumentVedlegg.builder()
                .kildeJournalpostId(sourceJournalpostIdSladdet)
                .dokumentInfoId(dokumentInfoIdSladdet.toString())
                .build());

        HttpHeaders headers = createHeadersWithUserAndServiceUserToken();

        TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

        HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
        var responseEntity = restTemplate.exchange(
                apiJournalpostPath(targetJournalpostId + TILKNYTT_VEDLEGG_PATH), PUT, requestHttpEntity, TilknyttVedleggResponse.class);

        assertThat(responseEntity.getStatusCode(), is(MULTI_STATUS));
        assertThat(responseEntity.getBody().getFeiledeDokumenter(), hasSize(2));
        assertThat(responseEntity.getBody().getFeiledeDokumenter().get(0).getArsakKode(), is(IKKE_FUNNET));
        assertThat(responseEntity.getBody().getFeiledeDokumenter().get(1).getArsakKode(), is(IKKE_FUNNET));
    }

    private void assertRelasjon(Long journalpostIdTilknyttet, DokumentInfo dokumentInfoKopi) {
        assertRelasjon(journalpostIdTilknyttet, dokumentInfoKopi, null);
    }

    private void assertRelasjon(Long journalpostIdTilknyttet, DokumentInfo dokumentInfoKopi, Integer rekkefoelge) {
        JournalpostDokumentInfoRelasjon tilknyttetRelasjon = dokumentInfoKopi.findJournalpostRelasjonByJournalpostId(journalpostIdTilknyttet);
        assertThat(tilknyttetRelasjon.getTilknyttetAvNavn(), is(PERSON_USER_NAME));
        assertThat(tilknyttetRelasjon.getOpprettetKildeNavn(), is(SERVICE_USER_ID));
        assertThat(tilknyttetRelasjon.getRekkefoelge(), is(rekkefoelge));
    }

    private void assertDokumentInfo(DokumentInfo sourceDokumentInfo, DokumentInfo dokumentInfoKopi) {
        assertEquals(sourceDokumentInfo.getDokumentstatus(), dokumentInfoKopi.getDokumentstatus());
        assertThat(sourceDokumentInfo.getDokumentFerdigDato()).isCloseTo(dokumentInfoKopi.getDokumentFerdigDato(), within(200, MILLIS));
        assertEquals(sourceDokumentInfo.getTittel(), dokumentInfoKopi.getTittel());
        assertEquals(sourceDokumentInfo.getBrevkode(), dokumentInfoKopi.getBrevkode());
        assertEquals(sourceDokumentInfo.getDokumenttypeId(), dokumentInfoKopi.getDokumenttypeId());
        assertEquals(sourceDokumentInfo.getBrevgruppe(), dokumentInfoKopi.getBrevgruppe());
        assertNotNull(dokumentInfoKopi.getOriginalJournalpost());
        assertEquals(sourceDokumentInfo.getOriginalJournalpost().getJournalpostId(), dokumentInfoKopi.getOriginalJournalpost().getJournalpostId());
        assertEquals(sourceDokumentInfo.getSensitivt(), dokumentInfoKopi.getSensitivt());
        assertEquals(sourceDokumentInfo.getKonvertertFraSystem(), dokumentInfoKopi.getKonvertertFraSystem());
        assertNull(dokumentInfoKopi.getEndretAvNavn());
        assertEquals(sourceDokumentInfo.getKassertAvNavn(), dokumentInfoKopi.getKassertAvNavn());
        assertEquals(sourceDokumentInfo.getDatoKassert(), dokumentInfoKopi.getDatoKassert());
        assertThat(dokumentInfoKopi.getOpprettetKildeNavn(), is(SERVICE_USER_ID));
        assertNull(dokumentInfoKopi.getEndretKildeNavn());
    }

    private void assertFildetaljer(FilDetaljer sourceFilDetaljer, FilDetaljer filDetaljerKopi) {
        assertEquals(sourceFilDetaljer.getFiltype(), filDetaljerKopi.getFiltype());
        assertEquals(sourceFilDetaljer.getOnDemandId(), filDetaljerKopi.getOnDemandId());
        assertEquals(sourceFilDetaljer.getOnDemandInstans(), filDetaljerKopi.getOnDemandInstans());
        assertEquals(sourceFilDetaljer.getMetaforceInstanceId(), filDetaljerKopi.getMetaforceInstanceId());
        assertThat(filDetaljerKopi.getVariantFormat(), is(ARKIV));
        assertThat(filDetaljerKopi.getOpprettetKildeNavn(), is(SERVICE_USER_ID));
        assertEquals(sourceFilDetaljer.getBatchNavn(), filDetaljerKopi.getBatchNavn());
        assertEquals(sourceFilDetaljer.getFilnavn(), filDetaljerKopi.getFilnavn());
        assertEquals(sourceFilDetaljer.getFilstorrelse(), filDetaljerKopi.getFilstorrelse());
        assertEquals(sourceFilDetaljer.getSkjermingType(), filDetaljerKopi.getSkjermingType());
        assertNull(filDetaljerKopi.getEndretKildeNavn());
    }

    private void assertDokumentFil(DokumentFil sourceDokumentFil, DokumentFil dokumentFilKopi) {
        assertEquals(new String(sourceDokumentFil.getFil()), new String(dokumentFilKopi.getFil()));
        assertThat(dokumentFilKopi.getOpprettetKildeNavn(), is(SERVICE_USER_ID));
    }

    private TilknyttVedleggRequest createTilknyttVedleggRequest(List<DokumentVedlegg> dokumentVedleggList) {
        return TilknyttVedleggRequest.builder()
                .tilknyttetAvNavn("TilknyttVedleggIT")
                .dokument(dokumentVedleggList)
                .build();
    }

    private Journalpost createJournalpostSladdet() {
        Journalpost journalpostSladdet = createFerdigstiltJournalpostWithHoveddokument();
        journalpostSladdet.setJournalstatus(J);
        journalpostSladdet.setJournalposttype(U);
        journalpostSladdet.setOpprettetAvNavn("opprettetAvNavn");
        journalpostSladdet.setOpprettetKildeNavn("opprettetKildeNavn");
        journalpostSladdet.setEndretKildeNavn("endretKildeNavn");
        journalpostSladdet.setEndretAvNavn("endretAvNavn");
        journalpostSladdet.setKanalReferanseId(KANAL_REFERANSE_ID + UUID.randomUUID());

        DokumentInfo dokumentInfo = journalpostSladdet.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
        dokumentInfo.removeFilDetaljer(dokumentInfo.findFilDetaljerByVariantFormat(PRODUKSJON));
        dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, SLADDET));
        return journalpostSladdet;
    }

    private Journalpost createJournalpostArkiv(JournalpostTypeCode journalpostTypeCode) {
        Journalpost journalpostArkiv = createFerdigstiltJournalpostWithHoveddokument();
        journalpostArkiv.setJournalstatus(D);
        journalpostArkiv.setJournalposttype(journalpostTypeCode);
        journalpostArkiv.setOpprettetAvNavn("opprettetAvNavn");
        journalpostArkiv.setOpprettetKildeNavn("opprettetKildeNavn");
        journalpostArkiv.setEndretKildeNavn("endretKildeNavn");
        journalpostArkiv.setEndretAvNavn("endretAvNavn");
        journalpostArkiv.setKanalReferanseId(KANAL_REFERANSE_ID + UUID.randomUUID());

        DokumentInfo dokumentInfo = journalpostArkiv.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
        dokumentInfo.removeFilDetaljer(dokumentInfo.findFilDetaljerByVariantFormat(PRODUKSJON));
        return journalpostArkiv;
    }

    private void completeCurrentAndStartNewTransaction() {
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }

    private static void generateAndStubSafResponse(Journalpost... journalposts) {
        String response = """
                {
                "data": {
                  "journalpost": {
                    "dokumenter": [
                """ +
                Stream.of(journalposts)
                        .map(Journalpost::findHoveddokumentDokumentInfoRelasjon)
                        .map(JournalpostDokumentInfoRelasjon::getDokumentInfo)
                        .map(DokumentInfo::getDokumentInfoId)
                        .map(id -> String.format("""
                                  {
                                  "dokumentInfoId": "%d",
                                  "dokumentvarianter": [
                                	{
                                	  "saksbehandlerHarTilgang": true,
                                	  "variantformat": "ARKIV"
                                	}
                                  ]
                                }""", id))
                        .collect(joining(","))
                + "] }}}";

        stubFor(post(urlMatching("/safgraphql"))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(response)));
    }

}
