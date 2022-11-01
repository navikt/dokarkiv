package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.EndreLogiskVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.LeggTilLogiskVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.LeggTilLogiskVedleggResponse;
import no.nav.dokarkiv.journalpost.v1.services.LogiskVedleggService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerEndreLogiskVedlegg;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerLeggTilLogiskVedlegg;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerSlettLogiskVedlegg;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.hasText;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateId;

@Tag(name="journalpostapi - logiske vedlegg", description = "Tjenester for å slette, endre og legge til logiske vedlegg")
@Slf4j
@Protected
@RestController
@Transactional
@RequestMapping("/rest/journalpostapi/v1/dokumentInfo")
public class JournalfoerSkannetDokumentRestController {

    private final LogiskVedleggService logiskVedleggService;

    private static final String DOKUMENT_INFO_ID_STRING = "dokumentInfoId";
    private static final String LOGISK_VEDLEGG_ID_STRING = "logiskVedleggId";
    private static final String TITTEL_STRING = "tittel";

    public JournalfoerSkannetDokumentRestController(final LogiskVedleggService logiskVedleggService) {
        this.logiskVedleggService = logiskVedleggService;
    }

    @SwaggerEndreLogiskVedlegg
    @PostMapping(value = "/{dokumentInfoId}/logiskVedlegg/{logiskVedleggId}")
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "endrelogiskvedlegg"}, percentiles = {0.5, 0.95})
    public ResponseEntity<String> endreLogiskVedlegg (
            @PathVariable String dokumentInfoId,
            @PathVariable String logiskVedleggId,
            @RequestBody EndreLogiskVedleggRequest request) {
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
        MDC.put(MDC_REQUEST_ID, "endrelogiskvedlegg");
        log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall om å endre logisk vedlegg med logiskVedleggId={} på dokument med dokumentInfoId={}",
                logiskVedleggId, dokumentInfoId);

        validateId(dokumentInfoId, DOKUMENT_INFO_ID_STRING);
        validateId(logiskVedleggId, LOGISK_VEDLEGG_ID_STRING);
        hasText(request.getTittel(), TITTEL_STRING);

        logiskVedleggService.endreLogiskVedlegg(dokumentInfoId, logiskVedleggId, request);

        log.info("endrelogiskvedlegg har endret logisk vedlegg med logiskVedleggId={}.", logiskVedleggId);
        return ResponseEntity.ok("Logisk vedlegg endret");
    }

    @SwaggerLeggTilLogiskVedlegg
    @PostMapping(value = "/{dokumentInfoId}/logiskVedlegg/")
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "leggtillogiskvedlegg"}, percentiles = {0.5, 0.95})
    public ResponseEntity<LeggTilLogiskVedleggResponse> leggTilLogiskVedlegg (
            @PathVariable String dokumentInfoId,
            @RequestBody LeggTilLogiskVedleggRequest request) {
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
        MDC.put(MDC_REQUEST_ID, "leggtillogiskvedlegg");
        log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall om å legge til logisk vedlegg på dokument med dokumentInfoId={}", dokumentInfoId);

        validateId(dokumentInfoId, DOKUMENT_INFO_ID_STRING);
        hasText(request.getTittel(), TITTEL_STRING);

        String logiskVedleggId = logiskVedleggService.leggTilLogiskVedlegg(dokumentInfoId, request);
        LeggTilLogiskVedleggResponse response = LeggTilLogiskVedleggResponse.builder().logiskVedleggId(logiskVedleggId).build();

        log.info("endrelogiskvedlegg har lagt til logisk vedlegg med logiskVedleggId={}.", logiskVedleggId);
        return ResponseEntity.ok(response);
    }

    @SwaggerSlettLogiskVedlegg
    @DeleteMapping(value = "/{dokumentInfoId}/logiskVedlegg/{logiskVedleggId}")
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "slettlogiskvedlegg"}, percentiles = {0.5, 0.95})
    public ResponseEntity<String> slettLogiskVedlegg (
            @PathVariable String dokumentInfoId,
            @PathVariable String logiskVedleggId) {
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
        MDC.put(MDC_REQUEST_ID, "slettlogiskvedlegg");
        log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall om å har mottatt kall om å slette logisk vedlegg med logiskVedleggId={} på dokument med dokumentInfoId={}", logiskVedleggId, dokumentInfoId);

        validateId(dokumentInfoId, DOKUMENT_INFO_ID_STRING);
        validateId(logiskVedleggId, LOGISK_VEDLEGG_ID_STRING);

        logiskVedleggService.slettLogiskVedlegg(dokumentInfoId, logiskVedleggId);

        log.info("slettlogiskvedlegg har slettet logisk vedlegg med logiskVedleggId={}.", logiskVedleggId);
        return ResponseEntity.ok("Logisk vedlegg slettet");
    }
}
