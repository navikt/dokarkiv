package no.nav.dokarkiv.journalpost.v1;


import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.PutOppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.PutOppdaterJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.rjoark200.OppdaterJournalpostService;
import no.nav.dokarkiv.journalpost.v1.rjoark200.util.Utils;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@RestController
@RequestMapping("/rest/v1/journalpost")
@Api
public class OppdaterJournalpostRestController {

	private final OppdaterJournalpostService oppdaterJournalpostService;
	private final AbacSecurityService abacSecurityService;

	public OppdaterJournalpostRestController(OppdaterJournalpostService oppdaterJournalpostService,
											 AbacSecurityService abacSecurityService) {
		this.abacSecurityService = abacSecurityService;
		this.oppdaterJournalpostService = oppdaterJournalpostService;
	}

	@Transactional
	@ResponseBody
	@PutMapping(value = "/{journalpostId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark200"}, percentiles = {0.5, 0.95})
	public PutOppdaterJournalpostResponse oppdaterJournalpost(
			@PathVariable String journalpostId,
			@RequestBody PutOppdaterJournalpostRequest request,
			@RequestHeader(value = AKSJONS_LOGG_HEADER, required = false) String aksjonsLoggHeaderString) throws UgyldigAksjonsLoggException {
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		log.info(String.format("rjoark200 har mottatt kall om å oppdatere journalpost med journalpostId=%s", journalpostId));
		Utils.validateId(journalpostId, "journalpostId");
		abacSecurityService.assertAccessToJournalpost(journalpostId);

		oppdaterJournalpostService.oppdaterJournalpost(journalpostId, request, aksjonsLoggHeaderString);

		log.info("rjoark200 har oppdatert journalpost med journalpostId={} i Joark.", journalpostId);
		return PutOppdaterJournalpostResponse.builder().journalpostId(journalpostId).build();
	}
}
