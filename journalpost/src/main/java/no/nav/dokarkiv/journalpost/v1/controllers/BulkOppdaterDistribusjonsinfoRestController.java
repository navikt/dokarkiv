package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.BulkOppdaterDistribusjonsinfoRequest;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.BulkOppdaterDistribusjonsinfoResponse;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.JournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.JournalpostResultResponse;
import no.nav.dokarkiv.journalpost.v1.services.OppdaterDistribusjonsinfoService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerBulkOppdaterDistribusjonsinfo;
import no.nav.dokarkiv.journalpost.v1.validators.OppdaterDistribusjonsinfoValidator;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.journalpost.v1.controllers.BulkOppdaterDistribusjonsinfoRestController.Result.FAILED;
import static no.nav.dokarkiv.journalpost.v1.controllers.BulkOppdaterDistribusjonsinfoRestController.Result.SUCCESS;

@Tag(name = "journalpostapi", description = "Tjenester for å oppdatere distribusjonsinfo")
@Slf4j
@Protected
@RestController
@RequestMapping("/rest/journalpostapi/v1/bulkOppdaterDistribusjonsinfo")
public class BulkOppdaterDistribusjonsinfoRestController {

	private final OppdaterDistribusjonsinfoService oppdaterDistribusjonsinfoService;
	private final OppdaterDistribusjonsinfoValidator oppdaterDistribusjonsinfoValidator;

	public BulkOppdaterDistribusjonsinfoRestController(OppdaterDistribusjonsinfoService oppdaterDistribusjonsinfoService,
													   OppdaterDistribusjonsinfoValidator oppdaterDistribusjonsinfoValidator) {
		this.oppdaterDistribusjonsinfoService = oppdaterDistribusjonsinfoService;
		this.oppdaterDistribusjonsinfoValidator = oppdaterDistribusjonsinfoValidator;
	}

	@Transactional
	@SwaggerBulkOppdaterDistribusjonsinfo
	@PostMapping
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "bulkOppdaterDistribusjonsinfo"}, percentiles = {0.5, 0.95})
	public ResponseEntity<BulkOppdaterDistribusjonsinfoResponse> oppdaterDistribusjonsinfo(@RequestBody BulkOppdaterDistribusjonsinfoRequest request) {
		try {
			MDC.put(MDC_REQUEST_ID, "bulkOppdaterDistribusjonsinfo");
			log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall for bulk oppdatering av distribusjonsinfo for {} journalposter", request.getJournalposter().size());

			RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

			Map<Result, List<JournalpostResponse>> results =  request.getJournalposter().stream()
					.map(journalpostWithDistribusjonsinfo -> {
						JournalpostResponse valideringsresultat = oppdaterDistribusjonsinfoValidator.validateRequest(journalpostWithDistribusjonsinfo);
						if (valideringsresultat.getErrormessage() != null ) {
							return valideringsresultat;
						}
						return oppdaterDistribusjonsinfoService.oppdaterDistribusjonsinfoFromBulk(journalpostWithDistribusjonsinfo);
					})
					.collect(Collectors.groupingBy(BulkOppdaterDistribusjonsinfoRestController::categoriseJournalpostProcessingResult, Collectors.toList()));

			results.getOrDefault(FAILED, emptyList()).stream()
					.map(result -> "Journalpost Feilet: journalpostId=%s, feilmelding=%s".formatted(result.getJournalpostId(), result.getErrormessage()))
					.forEach(log::warn);
			log.info(MDC.get(MDC_REQUEST_ID) + " har oppdatert distribusjonsinfo for {} journalposter (suksess: {}, feilet: {})",
					request.getJournalposter().size(), results.getOrDefault(SUCCESS, emptyList()).size(), results.getOrDefault(FAILED, emptyList()).size());
			return ResponseEntity.ok().body( new BulkOppdaterDistribusjonsinfoResponse(new JournalpostResultResponse(results.get(SUCCESS), results.get(FAILED))) );
		} finally {
			MDC.clear();
		}
	}

	private static Result categoriseJournalpostProcessingResult(JournalpostResponse journalpostResponse) {
		return journalpostResponse.getErrormessage() != null ? FAILED : SUCCESS;
	}

	enum Result {
		SUCCESS, FAILED
	}

}
