package no.nav.dokarkiv.journalpost.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTOMapper;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.FerdigstillJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.journalpost.FerdigstillJournalpostService;
import no.nav.dokarkiv.journalpost.v1.journalpost.KopierJournalpostService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFerdigstillJournalpost;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerKopierJournalpost;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;
import static no.nav.dokarkiv.journalpost.v1.journalpost.util.RequestUtils.validateId;
import static no.nav.dokarkiv.journalpost.v1.journalpost.util.RequestUtils.validateJournalfoerendeEnhet;

@Slf4j
@RestController
@RequestMapping("/rest/journalpostapi/v1/journalpost")
@Api
public class JournalpostRestController {

    private final FerdigstillJournalpostService ferdigstillJournalpostService;
    private final AbacSecurityService abacSecurityService;
    private final AksjonsLoggTOMapper aksjonsLoggTOMapper;
    private final JoarkRepository joarkRepository;
    private final KopierJournalpostService kopierJournalpostService;

    @Inject
    public JournalpostRestController(final FerdigstillJournalpostService ferdigstillJournalpostService,
                                                final AbacSecurityService abacSecurityService,
                                                final JoarkRepository joarkRepository,
                                                final KopierJournalpostService kopierJournalpostService) {
        this.kopierJournalpostService = kopierJournalpostService;
        this.ferdigstillJournalpostService = ferdigstillJournalpostService;
        this.abacSecurityService = abacSecurityService;
        this.aksjonsLoggTOMapper = new AksjonsLoggTOMapper();
        this.joarkRepository = joarkRepository;
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
        validateFerdigstillJournalpostRequest(journalpostId, request);
        abacSecurityService.assertAccessToJournalpost(journalpostId);
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

        List<ArkivElementEndringTO> arkivElementEndringTOList = ferdigstillJournalpostService.ferdigstill(journalpostId, request.getJournalfoerendeEnhet());

        log.info(MDC.get(MDC_REQUEST_ID) + " har ferdigstilt journalpost med journalpostId={}", journalpostId);

        return ResponseEntity.status(HttpStatus.OK).body("Journalpost ferdigstilt");
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

    private void validateFerdigstillJournalpostRequest(String journalpostId, FerdigstillJournalpostRequest request) {
        validateId(journalpostId, "journalpostId");
        validateJournalfoerendeEnhet(request.getJournalfoerendeEnhet(), "journalfoerendeEnhet");
    }

}
