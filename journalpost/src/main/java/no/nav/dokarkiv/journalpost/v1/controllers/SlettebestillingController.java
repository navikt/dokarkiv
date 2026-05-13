package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.SlettebestillingRequest;
import no.nav.dokarkiv.journalpost.v1.services.SlettebestillingService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerBestillSletting;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerOpphevBestillSletting;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping(path = "/rest/journalpostapi/v1/dokumentInfo/", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class SlettebestillingController {

	private final SlettebestillingService slettebestillingService;

	public SlettebestillingController(SlettebestillingService slettebestillingService) {
		this.slettebestillingService = slettebestillingService;
	}

	@SwaggerBestillSletting
	@PostMapping("{dokumentInfoId}/bestillSletting")
	public Long bestillSletting(
			@PathVariable @Parameter(description = "DokumentInfoId for dokumentet som skal slettes", required = true, example = "12345") long dokumentInfoId,
			@RequestBody SlettebestillingRequest slettebestillingRequest) {
		MDC.put(MDC_REQUEST_ID, "bestillsletting");
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		return slettebestillingService.bestillSletting(dokumentInfoId, slettebestillingRequest);
	}

	@SwaggerOpphevBestillSletting
	@PatchMapping("{dokumentInfoId}/opphevBestillSletting")
	public ResponseEntity<Void> opphevBestillSletting(
			@PathVariable @Parameter(description = "DokumentInfoId for dokumentet der bestilt sletting skal oppheves", required = true, example = "12345") long dokumentInfoId) {
		MDC.put(MDC_REQUEST_ID, "opphevBestillsletting");
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		slettebestillingService.opphevBestillSletting(dokumentInfoId);
		return ResponseEntity.noContent().build();
	}
}
