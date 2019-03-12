package no.nav.dokarkiv.journalpost.v1;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.CREATE_ACTION;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.rjoark202.OpprettJournalpostService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerOpprettJournalpost;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@Api
@Slf4j
@RestController
@RequestMapping("/rest/journalpostapi/v1/journalpost")
public class OpprettJournalpostRestController {

	private final AbacSecurityService abacSecurityService;
	private final OpprettJournalpostService service;

	@Inject
	public OpprettJournalpostRestController(final AbacSecurityService abacSecurityService,
											final OpprettJournalpostService opprettJournalpostService) {
		this.abacSecurityService = abacSecurityService;
		this.service = opprettJournalpostService;
	}

	@Transactional
	@PostMapping
	@SwaggerOpprettJournalpost
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = CREATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process", "rjoark202"}, percentiles = {0.5, 0.95}, histogram = true)
	public ResponseEntity<Long> opprettJournalpost(@RequestBody OpprettJournalpostRequest request) {
		MDC.put(MDC_REQUEST_ID, "rjoark202");
		log.info(MDC.get(MDC_REQUEST_ID) + " har mottat kall for opprettelse av ny journalpost");
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));

		// tilgangsstyring abac

		// validate request

		Long journalpostId = service.opprettJournalpost(request);
		log.info(MDC.get(MDC_REQUEST_ID) + " har opprettet ny journalpost med journalpostId={}", journalpostId);
		return ResponseEntity.status(HttpStatus.CREATED).body(journalpostId);
	}
}
