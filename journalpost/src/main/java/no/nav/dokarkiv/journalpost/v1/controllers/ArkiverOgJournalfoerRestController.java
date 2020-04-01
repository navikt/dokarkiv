package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.FerdigstillJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.FjernVedleggTilknyttetJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterDistribusjonsinfoRequest;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.DokumentInfo;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.services.FerdigstillJournalpostService;
import no.nav.dokarkiv.journalpost.v1.services.FjernVedleggTilknyttetJournalpost;
import no.nav.dokarkiv.journalpost.v1.services.OppdaterDistribusjonsinfoService;
import no.nav.dokarkiv.journalpost.v1.services.OppdaterJournalpostService;
import no.nav.dokarkiv.journalpost.v1.services.OpprettJournalpostService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFerdigstillJournalpost;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFjernVedlegg;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerOppdaterDistribusjonsinfo;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerOppdaterJournalpost;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerOpprettJournalpost;
import no.nav.dokarkiv.journalpost.v1.validators.FerdigstillJournalpostValidator;
import no.nav.dokarkiv.journalpost.v1.validators.OppdaterDistribusjonsinfoValidator;
import no.nav.dokarkiv.journalpost.v1.validators.OpprettJournalpostRequestValidator;
import no.nav.freg.abac.core.annotation.Abac;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.ARKIV_V2;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.CREATE_ACTION;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateId;

@Api(description = "Tjenester for å arkivere og journalføre i fagarkiv")
@Slf4j
@RestController
@RequestMapping("/rest/journalpostapi/v1/journalpost")
public class ArkiverOgJournalfoerRestController {

    private static final String TRUE = "true";
    private final FerdigstillJournalpostService ferdigstillJournalpostService;
    private final AbacSecurityService abacSecurityService;
    private final OppdaterJournalpostService oppdaterJournalpostService;
    private final OppdaterDistribusjonsinfoService oppdaterDistribusjonsinfoService;
    private final OpprettJournalpostService opprettJournalpostService;
    private final OpprettJournalpostRequestValidator opprettJournalpostRequestValidator;
    private final FerdigstillJournalpostValidator ferdigstillJournalpostValidator;
    private final OppdaterDistribusjonsinfoValidator oppdaterDistribusjonsinfoValidator;
    private final FjernVedleggTilknyttetJournalpost fjernVedleggTilknyttJournalpost;

    @Inject
    public ArkiverOgJournalfoerRestController(final FerdigstillJournalpostService ferdigstillJournalpostService,
                                              final OppdaterJournalpostService oppdaterJournalpostService,
                                              final OpprettJournalpostService opprettJournalpostService,
                                              final OppdaterDistribusjonsinfoService oppdaterDistribusjonsinfoService,
                                              final AbacSecurityService abacSecurityService,
                                              final FjernVedleggTilknyttetJournalpost fjernVedleggTilknyttJournalpost) {
        this.ferdigstillJournalpostService = ferdigstillJournalpostService;
        this.abacSecurityService = abacSecurityService;
        this.oppdaterJournalpostService = oppdaterJournalpostService;
        this.opprettJournalpostService = opprettJournalpostService;
        this.fjernVedleggTilknyttJournalpost = fjernVedleggTilknyttJournalpost;
        this.oppdaterDistribusjonsinfoService = oppdaterDistribusjonsinfoService;
        this.opprettJournalpostRequestValidator = new OpprettJournalpostRequestValidator();
        this.ferdigstillJournalpostValidator = new FerdigstillJournalpostValidator();
        this.oppdaterDistribusjonsinfoValidator = new OppdaterDistribusjonsinfoValidator();
    }

    @Transactional
    @SwaggerFerdigstillJournalpost
    @PatchMapping("/{journalpostId}/ferdigstill")
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST),
            @Abac.Attr(key = RESOURCE_FELLES_DOMENE, value = ARKIV_V2)},
            actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark201"}, percentiles = {0.5, 0.95})
    public ResponseEntity<String> ferdigstillJournalpost(
            @PathVariable @ApiParam(value = "IDen til journalposten som skal ferdigstilles", required = true, example = "77778888") String journalpostId,
            @RequestBody FerdigstillJournalpostRequest request) {
        MDC.put(MDC_REQUEST_ID, "rjoark201");
        log.info(MDC.get(MDC_REQUEST_ID) + " har mottat kall for ferdigstilling av journalpost med journalpostId={}", journalpostId);
        ferdigstillJournalpostValidator.validateRequest(journalpostId, request);
        abacSecurityService.assertAccessToJournalpost(journalpostId);
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

        ferdigstillJournalpostService.ferdigstill(Long.parseLong(journalpostId), request.getJournalfoerendeEnhet());
        log.info(MDC.get(MDC_REQUEST_ID) + " har ferdigstilt journalpost med journalpostId={}", journalpostId);

        return ResponseEntity.ok().body("Journalpost ferdigstilt");
    }

    @Transactional
    @SwaggerOppdaterDistribusjonsinfo
    @PatchMapping("/{journalpostId}/oppdaterDistribusjonsinfo")
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST),
            @Abac.Attr(key = RESOURCE_FELLES_DOMENE, value = ARKIV_V2)},
            actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark201"}, percentiles = {0.5, 0.95})
    public ResponseEntity<String> oppdaterDistribusjonsinfo(
            @PathVariable @ApiParam(value = "IDen til journalposten som skal oppdateres", required = true, example = "77778888") String journalpostId,
            @RequestBody OppdaterDistribusjonsinfoRequest request) {
        MDC.put(MDC_REQUEST_ID, "oppdaterDistribusjonsinfo");
        log.info(MDC.get(MDC_REQUEST_ID) + " har mottat kall for oppdatering av distribusjonsinfo for journalpostId={}", journalpostId);
        validateId(journalpostId, "journalpostId");
        oppdaterDistribusjonsinfoValidator.validateRequest(journalpostId, request);
        abacSecurityService.assertAccessToJournalpost(journalpostId);
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

        oppdaterDistribusjonsinfoService.oppdaterDistribusjonsinfo(Long.parseLong(journalpostId), request);

        log.info(MDC.get(MDC_REQUEST_ID) + " har oppdatert distribusjonsinfo på journalpost med journalpostId={}", journalpostId);

        return ResponseEntity.ok().body("Journalpost oppdatert");
    }

    @Transactional
    @SwaggerOppdaterJournalpost
    @ResponseBody
    @PutMapping(value = "/{journalpostId}")
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST),
            @Abac.Attr(key = RESOURCE_FELLES_DOMENE, value = ARKIV_V2)},
            actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "oppdaterjournalpost"}, percentiles = {0.5, 0.95})
    public OppdaterJournalpostResponse oppdaterJournalpost(
            @ApiParam(name = "journalpostId", value = "Angir JournalpostId som skal oppdatere f.eks. 467011764",
                    required = true, defaultValue = "467011764")
            @PathVariable String journalpostId,
            @RequestBody OppdaterJournalpostRequest request) {
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
        MDC.put(MDC_REQUEST_ID, "oppdaterjournalpost");
        log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall om å oppdatere journalpost med journalpostId={}", journalpostId);
        validateId(journalpostId, "journalpostId");
        abacSecurityService.assertAccessToJournalpost(journalpostId);

        oppdaterJournalpostService.oppdaterJournalpost(Long.parseLong(journalpostId), request);

        log.info("oppdaterjournalpost har oppdatert journalpost med journalpostId={} i Joark.", journalpostId);
        return OppdaterJournalpostResponse.builder().journalpostId(journalpostId).build();
    }

    @Transactional
    @PostMapping
    @SwaggerOpprettJournalpost
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST),
            @Abac.Attr(key = RESOURCE_FELLES_DOMENE, value = ARKIV_V2)},
            actions = @Abac.Attr(key = ACTION_ID, value = CREATE_ACTION))
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark202"}, percentiles = {0.5, 0.95}, histogram = true)
    public ResponseEntity<OpprettJournalpostResponse> opprettJournalpost(
            @RequestBody OpprettJournalpostRequest request,
            @ApiParam(name = "forsoekFerdigstill", value = "Angir hvorvidt tjenesten skal forsøke å ferdigstille eller ikke. Dette vil å sette journalposten i en status som indikerer at journalføring er komplett, \n og låser journalposten for senere endringer. " +
                    "Journalposten blir uansett opprettet, men kun ferdigstilt dersom den oppfyller krav til struktur og metadata som beskrevet under ferdigstillJournalpost.\n " +
                    "Sjekk \"journalpostferdigstilt\" på responsen for å være sikker på at journalposten faktisk ble ferdigstilt.", allowableValues = "true, false", required = false)
            @RequestParam(required = false) String forsoekFerdigstill) {
        MDC.put(MDC_REQUEST_ID, "rjoark202");
        log.info(MDC.get(MDC_REQUEST_ID) + " har mottat kall for opprettelse av ny journalpost");
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));

        // tilgangsstyring abac?

        try {
            opprettJournalpostRequestValidator.validateRequest(request);
        } catch (InputValideringFeiletException e) {
            log.warn("rjoark202 feilet under validering. " + e.getMessage(), e);
            throw e;
        }

        Journalpost journalpost = opprettJournalpostService.opprettJournalpost(request);

        List<DokumentInfo> dokumenter = new ArrayList<>();
        journalpost.getJournalpostDokumentInfoRelasjoner().forEach(
                journalpostDokumentInfoRelasjon -> dokumenter.add(DokumentInfo.builder()
                        .dokumentInfoId(journalpostDokumentInfoRelasjon.getDokumentInfo()
                                .getDokumentInfoId()
                                .toString())
                        .build())
        );

        Long journalpostId = journalpost.getJournalpostId();

        Optional<Pair<String, String>> ferdigstillResponse = Optional.empty();
        if (TRUE.equalsIgnoreCase(forsoekFerdigstill)) {
            ferdigstillResponse = Optional.of(ferdigstillJournalpostService.forsoekFerdigstill(journalpostId, request));
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(OpprettJournalpostResponse.builder()
                        .journalpostId(String.valueOf(journalpostId))
                        .journalstatus(ferdigstillResponse.map(Pair::getKey).orElse(journalpost.getJournalstatus().name()))
                        .melding(ferdigstillResponse.map(Pair::getValue).orElse(null))
                        .journalpostferdigstilt(ferdigstillResponse.map(Pair::getKey)
                                .filter("ENDELIG"::equalsIgnoreCase)
                                .isPresent())
                        .dokumenter(dokumenter)
                        .build());
    }

    @Transactional
    @SwaggerFjernVedlegg
    @PatchMapping("/{journalpostId}/fjernVedlegg")
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST),
            @Abac.Attr(key = RESOURCE_FELLES_DOMENE, value = ARKIV_V2)},
            actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "fjernVedleggTilknyttetJournalpost"}, percentiles = {0.5, 0.95})
    public ResponseEntity<String> fjernVedleggTilknyttetJournalpost(@PathVariable String journalpostId,
                                                                    @RequestBody FjernVedleggTilknyttetJournalpostRequest request) {
        MDC.put(MDCConstants.MDC_REQUEST_ID, "fjernVedleggTilknyttetJournalpost");
        validateId(journalpostId, "tilknyttJournalpostId");
        abacSecurityService.assertAccessToJournalpost(journalpostId);
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
        log.info("Fjerne vedlegg med dokumentinfoId={} som er knyttet til journalpost med journalpostId={}", request.getDokumentId(), journalpostId);
        fjernVedleggTilknyttJournalpost.fjernVedleggTilknyttetJournalpost(journalpostId, request);
        log.info("Vedlegg med dokumentinfoId={} som er knyttet til journalpost med journalpostId={} er fjernet", request.getDokumentId(), journalpostId);
        return ResponseEntity.ok("Vedlegg som knyttet til journalposten fjernet");
    }

}
