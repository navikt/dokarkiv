package no.nav.dokarkiv.journalpost.v1;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.CREATE_ACTION;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateId;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.FerdigstillJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.api.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.OpprettJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.services.OppdaterJournalpostService;
import no.nav.dokarkiv.journalpost.v1.services.FerdigstillJournalpostService;
import no.nav.dokarkiv.journalpost.v1.services.OpprettJournalpostService;
import no.nav.dokarkiv.journalpost.v1.validators.FerdigstillJournalpostValidator;
import no.nav.dokarkiv.journalpost.v1.validators.OpprettJournalpostRequestValidator;
import no.nav.dokarkiv.journalpost.v1.services.KopierJournalpostService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFerdigstillJournalpost;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerKopierJournalpost;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerOppdaterJournalpost;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerOpprettJournalpost;
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
import java.util.Optional;

@Api
@Slf4j
@RestController
@RequestMapping("/rest/journalpostapi/v1/journalpost")
public class JournalpostRestController {

    private final KopierJournalpostService kopierJournalpostService;
    private final FerdigstillJournalpostService ferdigstillJournalpostService;
    private final AbacSecurityService abacSecurityService;
    private final OppdaterJournalpostService oppdaterJournalpostService;
    private final OpprettJournalpostService opprettJournalpostService;
    private final OpprettJournalpostRequestValidator opprettJournalpostRequestValidator;
    private final FerdigstillJournalpostValidator ferdigstillJournalpostValidator;

    private static final String TRUE = "true";

    @Inject
    public JournalpostRestController(final FerdigstillJournalpostService ferdigstillJournalpostService,
                                     final KopierJournalpostService kopierJournalpostService,
                                     final OppdaterJournalpostService oppdaterJournalpostService,
                                     final OpprettJournalpostService opprettJournalpostService,
                                     final AbacSecurityService abacSecurityService) {
        this.ferdigstillJournalpostService = ferdigstillJournalpostService;
        this.kopierJournalpostService = kopierJournalpostService;
        this.abacSecurityService = abacSecurityService;
        this.oppdaterJournalpostService = oppdaterJournalpostService;
        this.opprettJournalpostService = opprettJournalpostService;
        this.opprettJournalpostRequestValidator = new OpprettJournalpostRequestValidator();
        this.ferdigstillJournalpostValidator = new FerdigstillJournalpostValidator();
    }

    @Transactional
    @SwaggerKopierJournalpost
    @PostMapping("/{journalpostId}/kopierJournalpost")
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark203"}, percentiles = {0.5, 0.95})
    public ResponseEntity<Long> kopierJournalpost(
            @ApiParam(value = "IDen til journalposten som skal kopieres", required = true, example = "77778888") @PathVariable String journalpostId) {
        MDC.put(MDC_REQUEST_ID, "rjoark203");
        log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall for kopiering av journalpost med journalpostId={}", journalpostId);
        validateId(journalpostId, "journalpostId");
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

        Long nyJournalpostId = kopierJournalpostService.execute(Long.parseLong(journalpostId));

        return ResponseEntity.status(HttpStatus.CREATED).body(nyJournalpostId);
    }

    @Transactional
    @SwaggerFerdigstillJournalpost
    @PatchMapping("/{journalpostId}/ferdigstill")
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
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
    @SwaggerOppdaterJournalpost
    @ResponseBody
    @PutMapping(value = "/{journalpostId}")
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)},
            actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "oppdaterjournalpost"}, percentiles = {0.5, 0.95})
    public OppdaterJournalpostResponse oppdaterJournalpost(
            @PathVariable String journalpostId,
            @RequestBody OppdaterJournalpostRequest request) throws UgyldigAksjonsLoggException {
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
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
            actions = @Abac.Attr(key = ACTION_ID, value = CREATE_ACTION))
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark202"}, percentiles = {0.5, 0.95}, histogram = true)
    public ResponseEntity<OpprettJournalpostResponse> opprettJournalpost(
            @RequestBody OpprettJournalpostRequest request,
            @ApiParam(name = "forsoekFerdigstill", allowableValues = "true, false", required = false)
            @RequestParam(required = false) String forsoekFerdigstill) {
        MDC.put(MDC_REQUEST_ID, "rjoark202");
        log.info(MDC.get(MDC_REQUEST_ID) + " har mottat kall for opprettelse av ny journalpost");
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));

        // tilgangsstyring abac?

        opprettJournalpostRequestValidator.validateRequest(request);

        Long journalpostId = opprettJournalpostService.opprettJournalpost(request);
        log.info(MDC.get(MDC_REQUEST_ID) + " har opprettet ny journalpost, journalpostId={}", journalpostId);

        Optional<Pair<String, String>> ferdigstillResponse = Optional.empty();
        if (TRUE.equalsIgnoreCase(forsoekFerdigstill)) {
            ferdigstillResponse = Optional.of(ferdigstillJournalpostService.forsoekFerdigstill(journalpostId, request));
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(OpprettJournalpostResponse.builder()
                        .journalpostId(String.valueOf(journalpostId))
                        .journalstatus(ferdigstillResponse.map(Pair::getKey).orElse("MIDLERTIDIG"))
                        .melding(ferdigstillResponse.map(Pair::getValue).orElse(null))
                        .build());
    }

}
