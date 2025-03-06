package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.MottaDokumentUtgaaendeSkanningRequest;
import no.nav.dokarkiv.journalpost.v1.services.MottaDokumentUtgaaendeSkanningService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerMottaDokumentUtgaaendeSkanning;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateIdAndParse;

@Tag(name = "journalpostapi - internt", description = "Interne tjenester mot journalpost")
@Slf4j
@Protected
@RestController
@RequestMapping("/rest/internal/journalpostapi/v1")
public class JournalpostInternProtectedRestController {
	private final MottaDokumentUtgaaendeSkanningService mottaDokumentUtgaaendeSkanningService;

	public JournalpostInternProtectedRestController(MottaDokumentUtgaaendeSkanningService mottaDokumentUtgaaendeSkanningService) {
		this.mottaDokumentUtgaaendeSkanningService = mottaDokumentUtgaaendeSkanningService;
	}

	@Protected
	@Transactional
	@SwaggerMottaDokumentUtgaaendeSkanning
	@PutMapping("/journalpost/{journalpostId}/mottaDokumentUtgaaendeSkanning")
	public ResponseEntity<Long> mottaDokumentUtgaaendeSkanning(
			@PathVariable String journalpostId,
			@RequestBody MottaDokumentUtgaaendeSkanningRequest request) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		long journalpostIdParsed = validateIdAndParse(journalpostId, "journalpostId");
		try {
			MDC.put(MDC_REQUEST_ID, "mottaDokumentUtgaaendeSkanning");
			log.info("mottaDokumentUtgaaendeSkanning har mottatt kall med journalpostId={}", journalpostIdParsed);

			mottaDokumentUtgaaendeSkanningService.mottaDokumentUtgaaendeSkanning(journalpostIdParsed, request);

			log.info("mottaDokumentUtgaaendeSkanning har lagt fildetaljer på journalpostId={}", journalpostIdParsed);

			return ResponseEntity.ok().build();
		} catch (DokarkivFunctionalException e) {
			log.warn("mottaDokumentUtgaaendeSkanning - feilet funksjonelt ved mottak av utgaaende skanning for journalpostId={}. Feilmelding={}",
					journalpostIdParsed, e.getMessage());
			throw e;
		} catch (DokarkivTechnicalException e) {
			log.error("mottaDokumentUtgaaendeSkanning - feilet teknisk ved mottak av utgaaende skanning for journalpostId={}. Feilmelding={}",
					journalpostIdParsed, e.getMessage());
			throw e;
		}
	}

}
