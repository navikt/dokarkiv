package no.nav.dokarkiv.journalpost.v1;

import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.journalpost.v1.rjoark201.util.RequestUtils.validateId;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.journalpost.v1.api.KopierJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.rjoark203.KopierJournalpostService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerKopierJournalpost;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api
@Slf4j
@RestController
@RequestMapping("/rest/journalpostapi/v1/journalpost")
public class KopierJournalpostRestController {

	private final KopierJournalpostService kopierJournalpostService;

	public KopierJournalpostRestController(final KopierJournalpostService kopierJournalpostService) {
		this.kopierJournalpostService = kopierJournalpostService;
	}

	@Transactional
	@SwaggerKopierJournalpost
	@PostMapping("/{journalpostId}/kopierJournalpost")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark203"}, percentiles = {0.5, 0.95})
	public ResponseEntity<String> kopierJournalpost(
			@RequestBody KopierJournalpostRequest request,
			@ApiParam(value = "IDen til journalposten som skal kopieres", required = true, example = "77778888") @PathVariable String journalpostId) {
		MDC.put(MDC_REQUEST_ID, "rjoark203");
		log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall for kopiering av journalpost med journalpostId={}", journalpostId);
		validateId(journalpostId, "journalpostId");

		kopierJournalpostService.execute(request);

		return ResponseEntity.ok("ok");
	}
}
