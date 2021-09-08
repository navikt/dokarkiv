package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.FerdigstillJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.FjernVedleggTilknyttetJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterDistribusjonsinfoRequest;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.DokumentInfoId;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostResult;
import no.nav.dokarkiv.journalpost.v1.bidrag.BidragService;
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
import no.nav.security.token.support.core.api.Protected;
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

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateId;
import static no.nav.dokarkiv.journalpost.v1.validators.OpprettJournalpostRequestValidator.MASKINELL_JOURNALFOERENDE_ENHET;

@Api(description = "Tjenester for å arkivere og journalføre i fagarkiv")
@Slf4j
@Protected
@RestController
@RequestMapping("/rest/journalpostapi/v1/journalpost")
public class ArkiverOgJournalfoerRestController {

    private static final String TRUE = "true";
    private static final String MIDLERTIDIG = "MIDLERTIDIG";
    private static final String STATUS_ENDELIG = "ENDELIG";
    private final FerdigstillJournalpostService ferdigstillJournalpostService;
    private final OppdaterJournalpostService oppdaterJournalpostService;
    private final OppdaterDistribusjonsinfoService oppdaterDistribusjonsinfoService;
    private final OpprettJournalpostService opprettJournalpostService;
    private final OpprettJournalpostRequestValidator opprettJournalpostRequestValidator;
    private final FerdigstillJournalpostValidator ferdigstillJournalpostValidator;
    private final OppdaterDistribusjonsinfoValidator oppdaterDistribusjonsinfoValidator;
    private final FjernVedleggTilknyttetJournalpost fjernVedleggTilknyttJournalpost;
    private final BidragService bidragService;

    @Inject
    public ArkiverOgJournalfoerRestController(final FerdigstillJournalpostService ferdigstillJournalpostService,
                                              final OppdaterJournalpostService oppdaterJournalpostService,
                                              final OpprettJournalpostService opprettJournalpostService,
                                              final OppdaterDistribusjonsinfoService oppdaterDistribusjonsinfoService,
                                              final FjernVedleggTilknyttetJournalpost fjernVedleggTilknyttJournalpost,
                                              final BidragService bidragService) {
        this.ferdigstillJournalpostService = ferdigstillJournalpostService;
        this.oppdaterJournalpostService = oppdaterJournalpostService;
        this.opprettJournalpostService = opprettJournalpostService;
        this.fjernVedleggTilknyttJournalpost = fjernVedleggTilknyttJournalpost;
        this.oppdaterDistribusjonsinfoService = oppdaterDistribusjonsinfoService;
        this.opprettJournalpostRequestValidator = new OpprettJournalpostRequestValidator();
        this.ferdigstillJournalpostValidator = new FerdigstillJournalpostValidator();
        this.oppdaterDistribusjonsinfoValidator = new OppdaterDistribusjonsinfoValidator();
        this.bidragService = bidragService;
    }

    @Transactional
    @SwaggerFerdigstillJournalpost
    @PatchMapping("/{journalpostId}/ferdigstill")
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark201"}, percentiles = {0.5, 0.95})
    public ResponseEntity<String> ferdigstillJournalpost(
            @PathVariable @ApiParam(value = "IDen til journalposten som skal ferdigstilles", required = true, example = "77778888") String journalpostId,
            @RequestBody FerdigstillJournalpostRequest request
    ) {
        MDC.put(MDC_REQUEST_ID, "rjoark201");
        log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall for ferdigstilling av journalpost med journalpostId={}", journalpostId);
        ferdigstillJournalpostValidator.validateRequest(journalpostId, request);
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

        ferdigstillJournalpostService.ferdigstill(Long.parseLong(journalpostId), request.getJournalfoerendeEnhet());
        log.info(MDC.get(MDC_REQUEST_ID) + " har ferdigstilt journalpost med journalpostId={}", journalpostId);

        return ResponseEntity.ok().body("Journalpost ferdigstilt");
    }

    @Transactional
    @SwaggerOppdaterDistribusjonsinfo
    @PatchMapping("/{journalpostId}/oppdaterDistribusjonsinfo")
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark201"}, percentiles = {0.5, 0.95})
    public ResponseEntity<String> oppdaterDistribusjonsinfo(
            @PathVariable @ApiParam(value = "IDen til journalposten som skal oppdateres", required = true, example = "77778888") String journalpostId,
            @RequestBody OppdaterDistribusjonsinfoRequest request) {
        MDC.put(MDC_REQUEST_ID, "oppdaterDistribusjonsinfo");
        log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall for oppdatering av distribusjonsinfo for journalpostId={}", journalpostId);
        validateId(journalpostId, "journalpostId");
        oppdaterDistribusjonsinfoValidator.validateRequest(journalpostId, request);
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

        oppdaterDistribusjonsinfoService.oppdaterDistribusjonsinfo(Long.parseLong(journalpostId), request);

        log.info(MDC.get(MDC_REQUEST_ID) + " har oppdatert distribusjonsinfo på journalpost med journalpostId={}", journalpostId);

        return ResponseEntity.ok().body("Journalpost oppdatert");
    }

    @Transactional
    @SwaggerOppdaterJournalpost
    @ResponseBody
    @PutMapping(value = "/{journalpostId}")
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

        oppdaterJournalpostService.oppdaterJournalpost(Long.parseLong(journalpostId), request);

        log.info("oppdaterjournalpost har oppdatert journalpost med journalpostId={} i Joark.", journalpostId);
        return OppdaterJournalpostResponse.builder().journalpostId(journalpostId).build();
    }

    @Transactional
    @PostMapping
    @SwaggerOpprettJournalpost
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark202"}, percentiles = {0.5, 0.95}, histogram = true)
    public ResponseEntity<OpprettJournalpostResponse> opprettJournalpost(
            @RequestBody OpprettJournalpostRequest request,
            @ApiParam(name = "forsoekFerdigstill", value = "Angir hvorvidt tjenesten skal forsøke å ferdigstille eller ikke. Dette vil å sette journalposten i en status som indikerer at journalføring er komplett, \n og låser journalposten for senere endringer. " +
                    "Journalposten blir uansett opprettet, men kun ferdigstilt dersom den oppfyller krav til struktur og metadata som beskrevet under ferdigstillJournalpost.\n " +
                    "Dersom det feiler å ferdigstille journalposten og den har status \"midlertidig\" og journalførendeEnhet==\"9999\" skal journalførendeEnhet settes til null." +
                    "Sjekk \"journalpostferdigstilt\" på responsen for å være sikker på at journalposten faktisk ble ferdigstilt.", allowableValues = "true, false", required = false)
            @RequestParam(required = false) String forsoekFerdigstill) {
        MDC.put(MDC_REQUEST_ID, "rjoark202");
        log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall for opprettelse av ny journalpost");
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));

        if ("dialogstyring-bidrag".equals(MDC.get(MDC_CONSUMER_ID))) {
            return bidragService.opprettBidrag(request);
        }

        try {
            opprettJournalpostRequestValidator.validateRequest(request, forsoekFerdigstill);
        } catch (InputValideringFeiletException e) {
            log.warn("rjoark202 feilet under validering. " + e.getMessage(), e);
            throw e;
        }

        OpprettJournalpostResult opprettJournalpostResult = opprettJournalpostService.opprettJournalpost(request);

        List<DokumentInfoId> dokumenter = new ArrayList<>();
        opprettJournalpostResult.getJournalpost().getJournalpostDokumentInfoRelasjoner().forEach(
                journalpostDokumentInfoRelasjon -> dokumenter.add(DokumentInfoId.builder()
                        .dokumentInfoId(journalpostDokumentInfoRelasjon.getDokumentInfo()
                                .getDokumentInfoId()
                                .toString())
                        .build())
        );

        Long journalpostId = opprettJournalpostResult.getJournalpost().getJournalpostId();
        HttpStatus httpStatus = opprettJournalpostResult.isAlreadyOpprettet() ? HttpStatus.CREATED : HttpStatus.CONFLICT;

        Optional<Pair<String, String>> ferdigstillResponse = Optional.empty();
        if (TRUE.equalsIgnoreCase(forsoekFerdigstill)) {
            ferdigstillResponse = Optional.of(ferdigstillJournalpostService.forsoekFerdigstill(journalpostId, request));
        }

        String journalForendeEnhetId = opprettJournalpostResult.getJournalpost().getJournalForendeEnhetId();
        String httpResponse = ferdigstillResponse.map(Pair::getKey).orElse(null);

        if(TRUE.equalsIgnoreCase(forsoekFerdigstill) && MASKINELL_JOURNALFOERENDE_ENHET.equals(journalForendeEnhetId) && MIDLERTIDIG.equals(httpResponse)) {
            ferdigstillJournalpostService.setJournalfoerendeEnhetNull(journalpostId,null);
        }


        return ResponseEntity
                .status(httpStatus)
                .body(OpprettJournalpostResponse.builder()
                        .journalpostId(String.valueOf(journalpostId))
                        .journalstatus(ferdigstillResponse.map(Pair::getKey).orElse(opprettJournalpostResult.getJournalpost().getJournalstatus().name()))
                        .melding(ferdigstillResponse.map(Pair::getValue).orElse(null))
                        .journalpostferdigstilt(ferdigstillResponse.map(Pair::getKey)
                                .filter(STATUS_ENDELIG::equalsIgnoreCase)
                                .isPresent())
                        .dokumenter(dokumenter)
                        .build());
    }

    @Transactional
    @SwaggerFjernVedlegg
    @PatchMapping("/{journalpostId}/fjernVedlegg")
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "fjernVedleggTilknyttetJournalpost"}, percentiles = {0.5, 0.95})
    public ResponseEntity<String> fjernVedleggTilknyttetJournalpost(@PathVariable String journalpostId,
                                                                    @RequestBody FjernVedleggTilknyttetJournalpostRequest request) {
        MDC.put(MDCConstants.MDC_REQUEST_ID, "fjernVedleggTilknyttetJournalpost");
        validateId(journalpostId, "tilknyttJournalpostId");
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
        log.info("Fjerne vedlegg med dokumentinfoId={} som er knyttet til journalpost med journalpostId={}", request.getDokumentId(), journalpostId);
        fjernVedleggTilknyttJournalpost.fjernVedleggTilknyttetJournalpost(journalpostId, request);
        log.info("Vedlegg med dokumentinfoId={} som er knyttet til journalpost med journalpostId={} er fjernet", request.getDokumentId(), journalpostId);
        return ResponseEntity.ok("Vedlegg som knyttet til journalposten fjernet");
    }

}
