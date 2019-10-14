package no.nav.dokarkiv.journalpost.v1.controllers;


import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.FjernVedleggTilknyttJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.services.FjernVedlaggTilknyttJournalpostService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFjernTilknyttVedlegg;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.ARKIV_V2;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateId;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

@Slf4j
@RestController
@RequestMapping("/rest/journalpostapi/v1/journalpost")
public class JournalpostRestController {


	private final FjernVedlaggTilknyttJournalpostService fjernVedlaggTilknyttJournalpostService;
	private final AbacSecurityService abacSecurityService;

	@Inject
	public JournalpostRestController(FjernVedlaggTilknyttJournalpostService fjernVedlaggTilknyttJournalpostService,
									 AbacSecurityService abacSecurityService) {
		this.fjernVedlaggTilknyttJournalpostService = fjernVedlaggTilknyttJournalpostService;
		this.abacSecurityService = abacSecurityService;
	}

	@Transactional
	@SwaggerFjernTilknyttVedlegg
	@PatchMapping("/{journalpostId}/fjernVedlegg")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST),
			@Abac.Attr(key = RESOURCE_FELLES_DOMENE, value = ARKIV_V2)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "fjernVedlaggTilknyttJournalpost"}, percentiles = {0.5, 0.95})
	public ResponseEntity<String> fjernVedlaggTilknyttJournalpost(@PathVariable String journalpostId,
																  @RequestBody FjernVedleggTilknyttJournalpostRequest request) {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "fjernVedlaggTilknyttJournalpost");
		validateId(journalpostId, "tilknyttJournalpostId");
		abacSecurityService.assertAccessToJournalpost(journalpostId);
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		log.info("Mottat kall til å fjerne vedlagg som knyttet til journalpost med journalpostId=%s, dokumentinfoId=%s", journalpostId, request.getDokumentId());
		fjernVedlaggTilknyttJournalpostService.fjernVedleggTilknyttJournalPost(journalpostId, request);
		log.info(String.format("Vedlegg som knyttet til journalpost med journalpostId=%s, dokumentinfoId=%s er fjernet",journalpostId,request.getDokumentId()));
		return ResponseEntity.ok("Vedlegg som knyttet til journalpost fjernet");
	}

}



