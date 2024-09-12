package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.journalpost.v1.api.avsluttSak.AvsluttSakRequest;
import no.nav.dokarkiv.journalpost.v1.api.avsluttSak.AvsluttSakValidator;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerAvsluttSak;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.journalpost.v1.api.avsluttSak.AvsluttSakValidator.validateAvsluttSakRequest;

@Slf4j
@Protected
@RestController
//@RequestMapping("/rest/journalpostapi/v1/sak")
@Tag(name = "journalpostapi - avsluttSak", description = "Tjeneste for å avslutte sak")
//TODO: Enable endpoint
public class AvsluttSakRestController {

	@Transactional
	@SwaggerAvsluttSak
//	@PatchMapping(value = "/avsluttsak")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "avslutSak"}, percentiles = {0.5, 0.95})
	public ResponseEntity<String> avsluttSak(
			@RequestBody AvsluttSakRequest avsluttSakRequest
	) {
		MDC.put(MDC_REQUEST_ID, "avsluttSak");

		validateAvsluttSakRequest(avsluttSakRequest);

		//TODO: add avsluttSakService and logic (neste pr)
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
