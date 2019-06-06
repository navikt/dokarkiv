package no.nav.dokarkiv.journalpost.v1.controllers;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.hasText;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateId;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.EndreLogiskVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.LeggTilLogiskVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.LeggTilLogiskVedleggResponse;
import no.nav.dokarkiv.journalpost.v1.services.LogiskVedleggService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerEndreLogiskVedlegg;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerLeggTilLogiskVedlegg;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerSlettLogiskVedlegg;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@Api(description = "Tjenester for å slette, endre og legge til logiske vedlegg")
@Slf4j
@RestController
@Transactional
@RequestMapping("/rest/journalpostapi/v1/dokumentInfo")
public class JournalfoerSkannetDokumentRestController {

    private final AbacSecurityService abacSecurityService;
    private final LogiskVedleggService logiskVedleggService;

    private static final String DOKUMENT_INFO_ID_STRING = "dokumentInfoId";
    private static final String LOGISK_VEDLEGG_ID_STRING = "logiskVedleggId";
    private static final String TITTEL_STRING = "tittel";

    @Inject
    public JournalfoerSkannetDokumentRestController(final AbacSecurityService abacSecurityService,
                                                    final LogiskVedleggService logiskVedleggService) {
        this.abacSecurityService = abacSecurityService;
        this.logiskVedleggService = logiskVedleggService;
    }

    @SwaggerEndreLogiskVedlegg
    @PostMapping(value = "/{dokumentInfoId}/logiskVedlegg/{logiskVedleggId}")
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)},
            actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
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

        abacSecurityService.assertAccessToDokumentInfo(Long.parseLong(dokumentInfoId));

        logiskVedleggService.endreLogiskVedlegg(dokumentInfoId, logiskVedleggId, request);

        log.info("endrelogiskvedlegg har endret logisk vedlegg med logiskVedleggId={}.", logiskVedleggId);
        return ResponseEntity.ok("Logisk vedlegg endret");
    }

    @SwaggerLeggTilLogiskVedlegg
    @PostMapping(value = "/{dokumentInfoId}/logiskVedlegg/")
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)},
            actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "leggtillogiskvedlegg"}, percentiles = {0.5, 0.95})
    public ResponseEntity<LeggTilLogiskVedleggResponse> leggTilLogiskVedlegg (
            @PathVariable String dokumentInfoId,
            @RequestBody LeggTilLogiskVedleggRequest request) {
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
        MDC.put(MDC_REQUEST_ID, "leggtillogiskvedlegg");
        log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall om å legge til logisk vedlegg på dokument med dokumentInfoId={}", dokumentInfoId);

        validateId(dokumentInfoId, DOKUMENT_INFO_ID_STRING);
        hasText(request.getTittel(), TITTEL_STRING);

        abacSecurityService.assertAccessToDokumentInfo(Long.parseLong(dokumentInfoId));

        String logiskVedleggId = logiskVedleggService.leggTilLogiskVedlegg(dokumentInfoId, request);
        LeggTilLogiskVedleggResponse response = LeggTilLogiskVedleggResponse.builder().logiskVedleggId(logiskVedleggId).build();

        log.info("endrelogiskvedlegg har lagt til logisk vedlegg med logiskVedleggId={}.", logiskVedleggId);
        return ResponseEntity.ok(response);
    }

    @SwaggerSlettLogiskVedlegg
    @DeleteMapping(value = "/{dokumentInfoId}/logiskVedlegg/{logiskVedleggId}")
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)},
            actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "slettlogiskvedlegg"}, percentiles = {0.5, 0.95})
    public ResponseEntity<String> slettLogiskVedlegg (
            @PathVariable String dokumentInfoId,
            @PathVariable String logiskVedleggId) {
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
        MDC.put(MDC_REQUEST_ID, "slettlogiskvedlegg");
        log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall om å har mottatt kall om å slette logisk vedlegg med logiskVedleggId={} på dokument med dokumentInfoId={}", logiskVedleggId, dokumentInfoId);

        validateId(dokumentInfoId, DOKUMENT_INFO_ID_STRING);
        validateId(logiskVedleggId, LOGISK_VEDLEGG_ID_STRING);

        abacSecurityService.assertAccessToDokumentInfo(Long.parseLong(dokumentInfoId));

        logiskVedleggService.slettLogiskVedlegg(dokumentInfoId, logiskVedleggId);

        log.info("slettlogiskvedlegg har slettet logisk vedlegg med logiskVedleggId={}.", logiskVedleggId);
        return ResponseEntity.ok("Logisk vedlegg slettet");
    }
}
