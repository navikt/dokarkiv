package no.nav.dokarkiv.ferdigstilljournalpost.v1;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;
import static no.nav.dokarkiv.ferdigstilljournalpost.v1.util.Utils.validateId;
import static no.nav.dokarkiv.ferdigstilljournalpost.v1.util.Utils.validateJournalfEnhet;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.ferdigstilljournalpost.v1.api.FerdigstillJournalpostRequest;
import no.nav.dokarkiv.ferdigstilljournalpost.v1.ferdigstill.FerdigstillJournalpostService;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@Slf4j
@RestController
@RequestMapping("/rest/ferdigstilljournalpost")
@Api(value = "FerdigstillJournalpost RestController")
public class FerdigstillJournalpostRestController {

	private final FerdigstillJournalpostService ferdigstillJournalpostService;
	private final AbacSecurityService abacSecurityService;

	@Inject
	public FerdigstillJournalpostRestController(final FerdigstillJournalpostService ferdigstillJournalpostService,
												final AbacSecurityService abacSecurityService) {
		this.ferdigstillJournalpostService = ferdigstillJournalpostService;
		this.abacSecurityService = abacSecurityService;
	}

	@Transactional
	@PatchMapping
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "ferdigstill"}, percentiles = {0.5, 0.95})
	@ResponseBody
	@ApiOperation("Ferdigstill journalpost")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok"),
			@ApiResponse(code = 400, message = "Kan ikke ferdigstille"),
			@ApiResponse(code = 500, message = "Internal server error")})
	public ResponseEntity<String> ferdigstillJournalpost(@RequestBody FerdigstillJournalpostRequest request) {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "ferdigstill_id");
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall for ferdigstilling av journalpost med journalpostId={}", request.getJournalpostId());
		validateRequest(request);
		abacSecurityService.assertAccessToJournalpost(request.getJournalpostId());
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		ferdigstillJournalpostService.ferdigstill(request.getJournalpostId(), request.getJournalfEnhet());

		return ResponseEntity.ok().body("Journalpost ferdigstilt");
	}

	private void validateRequest(FerdigstillJournalpostRequest request) {
		validateId(request.getJournalpostId(), "journalpostId");
		validateJournalfEnhet(request.getJournalfEnhet(), "journalfEnhet");
	}
}
