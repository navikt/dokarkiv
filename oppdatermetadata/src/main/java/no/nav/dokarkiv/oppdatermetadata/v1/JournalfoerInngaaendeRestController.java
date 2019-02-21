package no.nav.dokarkiv.oppdatermetadata.v1;


import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostResponse;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.oppdatermetadata.v1.temp001.UpdateInngaaendeJournalpostService;
import no.nav.dokarkiv.oppdatermetadata.v1.util.Utils;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;


/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@RestController
@RequestMapping("/rest/journalfoerinngaaende/v1/journalposter")
public class JournalfoerInngaaendeRestController {

	private final UpdateInngaaendeJournalpostService updateInngaaendeJournalpostService;
	private final AbacSecurityService abacSecurityService;

	@Inject
	public JournalfoerInngaaendeRestController(UpdateInngaaendeJournalpostService updateInngaaendeJournalpostService,
											   AbacSecurityService abacSecurityService) {
		this.abacSecurityService = abacSecurityService;
		this.updateInngaaendeJournalpostService = updateInngaaendeJournalpostService;
	}

	@Transactional
	@ResponseBody
	@PutMapping(value = "/{journalpostId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "temp001"}, percentiles = {0.5, 0.95})
	public PutJournalpostResponse updateInngaaendeJournalpost(@PathVariable String journalpostId, @RequestBody PutJournalpostRequest request) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		log.info(String.format("temp001 har mottatt kall om å oppdatere inngaaende journalpost med journalpostId=%s", journalpostId));
		Utils.validateId(journalpostId, "journalpostId");
		abacSecurityService.assertAccessToJournalpost(journalpostId);
		PutJournalpostResponse responseTo = updateInngaaendeJournalpostService.updateInngaaendeJournalpost(journalpostId, request);
		log.info("temp001 har oppdatert journalpost med journalpostId={} i Joark.", journalpostId);
		return responseTo;
	}
}
