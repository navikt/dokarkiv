package no.nav.dokarkiv.journalpost.v1;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;
import static no.nav.dokarkiv.journalpost.v1.util.RequestUtils.validateId;
import static no.nav.dokarkiv.journalpost.v1.util.RequestUtils.validateJournalfoerendeEnhet;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.FerdigstillJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.rjoark201.FerdigstillJournalpostService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFerdigstillJournalpost;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@Slf4j
@RestController
@RequestMapping("/rest/journalpostapi/v1/journalpost")
@Api
public class FerdigstillJournalpostRestController {

	private final FerdigstillJournalpostService ferdigstillJournalpostService;
	private final AbacSecurityService abacSecurityService;

	@Inject
	public FerdigstillJournalpostRestController(final FerdigstillJournalpostService ferdigstillJournalpostService,
												final AbacSecurityService abacSecurityService){
		this.ferdigstillJournalpostService = ferdigstillJournalpostService;
		this.abacSecurityService = abacSecurityService;
	}

	@Transactional
	@SwaggerFerdigstillJournalpost
	@PatchMapping("/{journalpostId}/ferdigstill")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark201"}, percentiles = {0.5, 0.95})
	public ResponseEntity<String> ferdigstillJournalpost(
			@RequestHeader(value = AKSJONS_LOGG_HEADER, required = false) String aksjonsLoggHeaderString,
			@PathVariable @ApiParam(value = "IDen til journalposten som skal ferdigstilles", required = true, example = "77778888") String journalpostId,
			@RequestBody FerdigstillJournalpostRequest request) throws UgyldigAksjonsLoggException {
		MDC.put(MDC_REQUEST_ID, "rjoark201");
		log.info(MDC.get(MDC_REQUEST_ID) + " har mottat kall for ferdigstilling av journalpost med journalpostId={}", journalpostId);
		validateRequest(journalpostId, request);
		abacSecurityService.assertAccessToJournalpost(journalpostId);
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		ferdigstillJournalpostService.ferdigstill(Long.parseLong(journalpostId), request.getJournalfoerendeEnhet(), aksjonsLoggHeaderString);
		log.info(MDC.get(MDC_REQUEST_ID) + " har ferdigstilt journalpost med journalpostId={}", journalpostId);

		return ResponseEntity.ok().body("Journalpost ferdigstilt");
	}

	private void validateRequest(String journalpostId, FerdigstillJournalpostRequest request) {
		validateId(journalpostId, "journalpostId");
		validateJournalfoerendeEnhet(request.getJournalfoerendeEnhet(), "journalfoerendeEnhet");
	}
}
