package no.nav.dokarkiv.journalpost.v1.itest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.MottaDokumentUtgaaendeSkanningRequest;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.util.Base64Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MottaDokumentUtgaaendeSkanningServiceIT extends AbstractJournalpostIT {
    private static final String GYLDIG_CONSUMER = "srvskanmotutgaaende";
    private static final String UGYLDIG_CONSUMER = "srvdokarkiv";
    private static final String NAV_CONSUMER_ID = "Nav-Consumer-Id";
    private static final String KILDE = "skanmotutgaaende";


    private final Date mockDate = new Date(Date.UTC(2000, Calendar.NOVEMBER, 10, 0, 0, 0));

    private final String mockMottaksKanal = MottaksKanalCode.SKAN_NETS.toString();
    private final List<Tilleggsopplysning> mockTilleggsopplysninger = List.of(new Tilleggsopplysning("mockNoekkel", "mockVerdi"));
    private final String mockBatchnavn = "mockBatchnavn";
    private final byte[] mockData = "mockData".getBytes();
    private final String mockFilnavn = "mockFilnavn";

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void mottaDokumentHappy() {
        Journalpost journalpost = generateTestJournalpost(
                        JournalpostTypeCode.U,
                        JournalStatusCode.R,
                        TilknyttetJournalpostSomCode.HOVEDDOKUMENT
                ).build();

        long journalpostId = saveJournalpost(journalpost).getId();

        endTransaction();

        HttpHeaders headers = createHeaders(GYLDIG_CONSUMER);

        MottaDokumentUtgaaendeSkanningRequest request = buildRequest(List.of(DokumentVariant
                .builder()
                .filtype(FilTypeCode.PDF.toString())
                .variantformat(VariantFormatCode.ORIGINAL.toString())
                .fysiskDokument(mockData)
                .filnavn(mockFilnavn)
                .build()
        ));

        HttpEntity<MottaDokumentUtgaaendeSkanningRequest> requestHttpEntity = new HttpEntity<>(request, headers);
        ResponseEntity responseEntity = restTemplate.exchange(
                URL_JOURNALPOST_INTERN + journalpostId + "/mottaDokumentUtgaaendeSkanning", HttpMethod.PUT, requestHttpEntity, String.class);

        endTransaction();

        Journalpost oppdatertJP = joarkRepository.findById(journalpostId).get();
        Map<String, String> tilleggsopplysninger = oppdatertJP.getTilleggsopplysninger();
        FilDetaljer filDetaljer = oppdatertJP.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getFildetaljerListe().iterator().next();
        DokumentFil dokumentfil = dokumentFilRepository.findByFilUuid(filDetaljer.getFilUuid());

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(JournalStatusCode.FL, oppdatertJP.getJournalstatus());
        assertEquals(mockTilleggsopplysninger.get(0).getVerdi(), tilleggsopplysninger.get(mockTilleggsopplysninger.get(0).getNokkel()));
        assertEquals(MottaksKanalCode.SKAN_NETS, oppdatertJP.getMottakskanal());
        assertEquals(KILDE, oppdatertJP.getEndretKildeNavn());
        assertEquals(mockDate, oppdatertJP.getMottattDato());
        assertEquals(FilTypeCode.PDF, filDetaljer.getFiltype());
        assertEquals(mockFilnavn, filDetaljer.getFilnavn());
        assertEquals(VariantFormatCode.ORIGINAL, filDetaljer.getVariantFormat());
        assertArrayEquals(mockData, dokumentfil.getFil());
        assertEquals(mockBatchnavn, filDetaljer.getBatchNavn());

    }

    @Test
    public void shouldAcceptMultipleFilesInRequest() {
        Journalpost journalpost = generateTestJournalpost(
                JournalpostTypeCode.U,
                JournalStatusCode.R,
                TilknyttetJournalpostSomCode.HOVEDDOKUMENT
        ).build();

        long journalpostId = saveJournalpost(journalpost).getId();

        endTransaction();

        HttpHeaders headers = createHeaders(GYLDIG_CONSUMER);

        MottaDokumentUtgaaendeSkanningRequest request = buildRequest(
            List.of(
                DokumentVariant
                    .builder()
                    .filtype(FilTypeCode.PDF.toString())
                    .variantformat(VariantFormatCode.ORIGINAL.toString())
                    .fysiskDokument(mockData)
                    .filnavn(mockFilnavn + "-1")
                    .build(),
                DokumentVariant
                    .builder()
                    .filtype(FilTypeCode.PDF.toString())
                    .variantformat(VariantFormatCode.ORIGINAL.toString())
                    .fysiskDokument(mockData)
                    .filnavn(mockFilnavn + "-2")
                    .build()
        ));

        HttpEntity<MottaDokumentUtgaaendeSkanningRequest> requestHttpEntity = new HttpEntity<>(request, headers);
        ResponseEntity responseEntity = restTemplate.exchange(
                URL_JOURNALPOST_INTERN + journalpostId + "/mottaDokumentUtgaaendeSkanning", HttpMethod.PUT, requestHttpEntity, String.class);

        endTransaction();

        Journalpost oppdatertJP = joarkRepository.findById(journalpostId).get();

        Set<FilDetaljer> filDetaljerSet = oppdatertJP.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getFildetaljerListe();
        List<FilDetaljer> filDetaljerList = filDetaljerSet.stream().sorted(Comparator.comparing(FilDetaljer::getFilnavn)).collect(Collectors.toList());

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(2, filDetaljerList.size());
        assertEquals(mockFilnavn + "-1", filDetaljerList.get(0).getFilnavn());
        assertEquals(mockFilnavn + "-2", filDetaljerList.get(1).getFilnavn());

    }

    @Test
    public void shouldReturnBadRequestWithInvalidRequest() throws IOException {
        String errorMessage = "mottaDokumentUtgaaendeSkanning feilet ved validering av request " +
                "journalpostId=%s " +
                "mottakskanal=SKAN_NETS " +
                "batchnavn=mockBatchnavn " +
                "feilmedling=Kan ikke validere request: " +
                "dokumentvarianter[0] har ugyldig filtype mockUgyldigFiltype, har ugyldig variantformat mockUgyldigVariantformat, mangler fysiskDokument";

        Journalpost journalpost = generateTestJournalpost(
                JournalpostTypeCode.U,
                JournalStatusCode.R,
                TilknyttetJournalpostSomCode.HOVEDDOKUMENT
        ).build();

        long journalpostId = saveJournalpost(journalpost).getId();


        endTransaction();

        HttpHeaders headers = createHeaders(GYLDIG_CONSUMER);

        MottaDokumentUtgaaendeSkanningRequest request = buildRequest(List.of(DokumentVariant
                .builder()
                .filtype("mockUgyldigFiltype")
                .variantformat("mockUgyldigVariantformat")
                .fysiskDokument(null)
                .filnavn(mockFilnavn)
                .build()
        ));

        HttpEntity<MottaDokumentUtgaaendeSkanningRequest> requestHttpEntity = new HttpEntity<>(request, headers);
        ResponseEntity responseEntity = restTemplate.exchange(
                URL_JOURNALPOST_INTERN + journalpostId + "/mottaDokumentUtgaaendeSkanning", HttpMethod.PUT, requestHttpEntity, String.class);
        endTransaction();
        JsonNode responseBody = mapper.readTree((String) responseEntity.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        assertEquals(String.format(errorMessage, journalpostId), responseBody.get("message").textValue());

    }

    @Test
    public void shouldReturnBadRequestWithInvalidJournalpost() throws InterruptedException {
        String errorMessage = "mottaDokumentUtgaaendeSkanning feilet ved validering av journalpost " +
                "journalpostId=%s " +
                "mottakskanal=SKAN_NETS " +
                "batchnavn=mockBatchnavn " +
                "feilmedling=Kan ikke validere journalpost: JournalpostType er ikke U eller N; JournalStatus er ikke R; Har mer enn ett DokumentInfo objekt; Har ikke hoveddokument";

        Journalpost journalpost = generateTestJournalpost(
                JournalpostTypeCode.I,
                JournalStatusCode.FL,
                TilknyttetJournalpostSomCode.VEDLEGG
        ).build();

        DokumentInfo dokumentInfo = DokumentInfo
                .builder()
                .build();
        dokumentInfo.setOpprettetKildeNavn("Itest");

        JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon
                .builder()
                .tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
                .journalpost(journalpost)
                .journalpostDokumentInfoRelasjonId(10L)
                .dokumentInfo(
                        dokumentInfo
                )
                .tilknyttetAvNavn("Itest")
                .build();

        journalpostDokumentInfoRelasjon.setOpprettetKildeNavn("Itest");


        journalpost.addJournalpostDokumentInfoRelasjon(
                journalpostDokumentInfoRelasjon
        );


        Journalpost journalpostInRepository = joarkRepository.save(journalpost);


        long journalpostId = journalpostInRepository.getId();

        endTransaction();

        HttpHeaders headers = createHeaders(GYLDIG_CONSUMER);

        MottaDokumentUtgaaendeSkanningRequest request = buildRequest(List.of(DokumentVariant
                .builder()
                .filtype(FilTypeCode.PDF.toString())
                .variantformat(VariantFormatCode.ORIGINAL.toString())
                .fysiskDokument(mockData)
                .filnavn(mockFilnavn)
                .build()
        ));


        HttpEntity<MottaDokumentUtgaaendeSkanningRequest> requestHttpEntity = new HttpEntity<>(request, headers);
        ResponseEntity responseEntity = restTemplate.exchange(
                URL_JOURNALPOST_INTERN + journalpostId + "/mottaDokumentUtgaaendeSkanning", HttpMethod.PUT, requestHttpEntity, String.class);

        try {
            JsonNode responseBody = mapper.readTree((String) responseEntity.getBody());

            assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
            assertEquals(String.format(errorMessage, journalpostId), responseBody.get("message").textValue());

        } catch (IOException e) {
            fail();
        }

    }

    @Test
    public void shouldReturnNotFoundIfJournalpostDoesNotExist() throws IOException {

        String errorMessage = "mottaDokumentUtgaaendeSkanning\n" + "journalpost med id 0 ikke funnet";
        long journalpostId = 0L;

        HttpHeaders headers = createHeaders(GYLDIG_CONSUMER);

        MottaDokumentUtgaaendeSkanningRequest request = buildRequest(List.of(DokumentVariant
                .builder()
                .filtype(FilTypeCode.PDF.toString())
                .variantformat(VariantFormatCode.ORIGINAL.toString())
                .fysiskDokument(mockData)
                .filnavn(mockFilnavn)
                .build()
        ));

        HttpEntity<MottaDokumentUtgaaendeSkanningRequest> requestHttpEntity = new HttpEntity<>(request, headers);
        ResponseEntity responseEntity = restTemplate.exchange(
                URL_JOURNALPOST_INTERN + journalpostId + "/mottaDokumentUtgaaendeSkanning", HttpMethod.PUT, requestHttpEntity, String.class);
        JsonNode responseBody = mapper.readTree((String) responseEntity.getBody());

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(errorMessage, responseBody.get("message").textValue());

    }

    @Test
    public void shouldReturnForbiddenIfInvalidConsumer() throws IOException {

        String errorMessage = "Konsument har ikke tilgang til å kalle tjenesten";
        long journalpostId = 0L;

        HttpHeaders headers = createHeaders(UGYLDIG_CONSUMER);

        MottaDokumentUtgaaendeSkanningRequest request = buildRequest(List.of(DokumentVariant
                .builder()
                .filtype(FilTypeCode.PDF.toString())
                .variantformat(VariantFormatCode.ORIGINAL.toString())
                .fysiskDokument(mockData)
                .filnavn(mockFilnavn)
                .build()
        ));

        HttpEntity<MottaDokumentUtgaaendeSkanningRequest> requestHttpEntity = new HttpEntity<>(request, headers);
        ResponseEntity responseEntity = restTemplate.exchange(
                URL_JOURNALPOST_INTERN + journalpostId + "/mottaDokumentUtgaaendeSkanning", HttpMethod.PUT, requestHttpEntity, String.class);
        JsonNode responseBody = mapper.readTree((String) responseEntity.getBody());


        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertEquals(errorMessage, responseBody.get("message").textValue());

    }

    private MottaDokumentUtgaaendeSkanningRequest buildRequest(List<DokumentVariant> dokumentVarianter){
        return new MottaDokumentUtgaaendeSkanningRequest(
                mockDate,
                mockMottaksKanal,
                mockTilleggsopplysninger,
                mockBatchnavn,
                dokumentVarianter
        );
    }

    private HttpHeaders createHeaders(String consumer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Nav-Consumer-Id", NAV_CONSUMER_ID);
        String token = Base64Utils.encodeToString(
                (consumer + ":" + "hemmelig").getBytes(StandardCharsets.UTF_8));
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + token);

        return headers;
    }

    private void endTransaction() {
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }

    public void saveFil(Set<FilDetaljer> fd) {
        fd.forEach(filDetaljer -> {
            DokumentFil dokumentFil = filDetaljer.createDokumentFil();
            dokumentFil.setOpprettetKildeNavn("kildenavn");
            dokumentFilRepository.save(dokumentFil);
        });
    }

    private JournalpostBuilder generateTestJournalpost(
            JournalpostTypeCode journalpostTypeCode,
            JournalStatusCode journalStatusCode,
            TilknyttetJournalpostSomCode tilknyttetJournalpostSomCode,
            FilDetaljer... filDetaljer
    ) {
        return JournalpostBuilder
                .getJournalpostBuilder()
                .journalpostId(0L)
                .opprettetKildeNavn("ITest")
                .journalpostType(journalpostTypeCode)
                .journalStatus(journalStatusCode)
                .dokumentInfoRelasjoner(
                        JournalpostDokumentInfoRelasjonBuilder
                                .getJournalpostDokumentInfoRelasjonBuilder()
                                .opprettetKildeNavn("ITest")
                                .tilknyttetAvNavn("ITest")
                                .dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder().opprettetKildeNavn("ITest").filDetaljerList(filDetaljer).build())
                                .tilknyttetJournalpostSom(tilknyttetJournalpostSomCode)
                                .build());
    }

}
