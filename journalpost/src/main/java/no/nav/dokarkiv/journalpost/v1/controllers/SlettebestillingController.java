package no.nav.dokarkiv.journalpost.v1.controllers;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.SlettebestillingRequest;
import no.nav.dokarkiv.journalpost.v1.services.SlettebestillingService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerBestillSletting;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Protected
@RestController
@RequestMapping(path = "/rest/journalpostapi/v1/bestillSletting", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class SlettebestillingController {

	private final SlettebestillingService slettebestillingService;

	public SlettebestillingController(SlettebestillingService slettebestillingService) {
		this.slettebestillingService = slettebestillingService;
	}

	@SwaggerBestillSletting
	@PostMapping
	public Long bestillSletting(@RequestBody SlettebestillingRequest slettebestillingRequest) {
		MDC.put(MDC_REQUEST_ID, "bestillsletting");
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		return slettebestillingService.bestillSletting(slettebestillingRequest);
	}
}
