package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.FinnMottatteJournalposterResponse;
import no.nav.dokarkiv.journalpost.v1.services.FinnMottatteJournalposterService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFinnMottatteJournalposterMedTemaEldreEnn;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;

@Tag(name="journalpostapi - internt", description = "Interne tjenester mot journalpost")
@Slf4j
@Protected
@RestController
@RequestMapping("/rest/internal/journalpostapi/v1")
public class JournalpostInternProtectedRestController {

	private final FinnMottatteJournalposterService finnMottatteJournalposterService;

	@Inject
	public JournalpostInternProtectedRestController(FinnMottatteJournalposterService finnMottatteJournalposterService){

		this.finnMottatteJournalposterService = finnMottatteJournalposterService;
	}

	@Transactional(readOnly = true)
	@SwaggerFinnMottatteJournalposterMedTemaEldreEnn
	@ResponseBody
	@GetMapping(value = "/finnMottatteJournalposter/{temaer}/{eldreEnn}")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "finnMottatteJournalposter"}, percentiles = {0.5, 0.95})
	@Protected
	public ResponseEntity<FinnMottatteJournalposterResponse> finnMottatteJournalposterMedTemaEldreEnn(
			@PathVariable("temaer") List<String> temaer,
			@PathVariable("eldreEnn") int eldreEnn) {
		MDC.put(MDC_REQUEST_ID, "finnMottatteJournalposter");
		try {

			log.info("finnMottatteJournalposter_eldreEnn har mottatt kall om å hente ubehandlede journalposter med tema blandt " + Arrays.toString(temaer.toArray()));

			FinnMottatteJournalposterResponse ubehandledeJournalposter = finnMottatteJournalposterService.finnMottatteJournalposterMedTemaEldreEnn(temaer, eldreEnn);

			return ResponseEntity
					.ok()
					.body(ubehandledeJournalposter);
		} catch (DokarkivFunctionalException e) {
			log.warn("finnMottatteJournalposter - feilet funksjonelt ved søk på ubehandlede journalposter med tema blandt {}. Feilmelding={}", Arrays.toString(temaer.toArray()), e
					.getMessage());
			throw e;
		} catch (DokarkivTechnicalException e) {
			log.error("finnMottatteJournalposter - feilet teknisk ved søk på ubehandlede journalposter med tema blandt {}. Feilmelding={}", Arrays.toString(temaer.toArray()), e
					.getMessage());
			throw e;
		}
	}

}
