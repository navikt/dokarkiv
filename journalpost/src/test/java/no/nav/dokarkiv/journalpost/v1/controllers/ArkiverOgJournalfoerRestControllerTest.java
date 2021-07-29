package no.nav.dokarkiv.journalpost.v1.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostResult;
import no.nav.dokarkiv.journalpost.v1.services.*;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArkiverOgJournalfoerRestControllerTest {

    @Mock
    private FerdigstillJournalpostService ferdigstillJournalpostService;

    @Mock
    private OppdaterJournalpostService oppdaterJournalpostService;

    @Mock
    private OpprettJournalpostService opprettJournalpostService;

    @Mock
    private OppdaterDistribusjonsinfoService oppdaterDistribusjonsinfoService;

    @Mock
    private FjernVedleggTilknyttetJournalpost fjernVedleggTilknyttJournalpost;

    @Test
    public void shouldProcessJsonString() throws IOException {
        Journalpost journalpost = new Journalpost(1L, 1L);
        journalpost.setJournalstatus(JournalStatusCode.FS);

        when(opprettJournalpostService.opprettJournalpost(any())).thenReturn(
                new OpprettJournalpostResult(journalpost, true)
        );
        Pair<String, String> ferdigstillResponse = new MutablePair<>();
        when(ferdigstillJournalpostService.forsoekFerdigstill(any(), any())).thenReturn(
                ferdigstillResponse
        );

        String json = Files.readString(Path.of("src/test/resources/request.json"));

        ArkiverOgJournalfoerRestController controller = new ArkiverOgJournalfoerRestController(
                ferdigstillJournalpostService,
                oppdaterJournalpostService,
                opprettJournalpostService,
                oppdaterDistribusjonsinfoService,
                fjernVedleggTilknyttJournalpost
        );

        ObjectMapper mapper = new ObjectMapper();
        OpprettJournalpostRequest request = mapper.readValue(json.getBytes(StandardCharsets.UTF_8), OpprettJournalpostRequest.class);

        MDC.put(MDC_CONSUMER_ID, "srvdokarkivproxy");
        controller.opprettJournalpost(request, "true");
    }

    @Test
    public void shouldProcessJsonBytes() throws IOException {
        Journalpost journalpost = new Journalpost(1L, 1L);
        journalpost.setJournalstatus(JournalStatusCode.FS);

        when(opprettJournalpostService.opprettJournalpost(any())).thenReturn(
                new OpprettJournalpostResult(journalpost, true)
        );
        Pair<String, String> ferdigstillResponse = new MutablePair<>();
        when(ferdigstillJournalpostService.forsoekFerdigstill(any(), any())).thenReturn(
                ferdigstillResponse
        );

        String base64 = Files.readString(Path.of("src/test/resources/request_base64.txt"));

        ArkiverOgJournalfoerRestController controller = new ArkiverOgJournalfoerRestController(
                ferdigstillJournalpostService,
                oppdaterJournalpostService,
                opprettJournalpostService,
                oppdaterDistribusjonsinfoService,
                fjernVedleggTilknyttJournalpost
        );

        ObjectMapper mapper = new ObjectMapper();
        OpprettJournalpostRequest request = mapper.readValue(Base64.getDecoder().decode(base64), OpprettJournalpostRequest.class);

        MDC.put(MDC_CONSUMER_ID, "srvdokarkivproxy");
        controller.opprettJournalpost(request, "true");
    }
}
