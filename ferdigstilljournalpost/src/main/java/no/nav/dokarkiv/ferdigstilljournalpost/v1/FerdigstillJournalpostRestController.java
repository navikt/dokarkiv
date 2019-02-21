package no.nav.dokarkiv.ferdigstilljournalpost.v1;

import static no.nav.dokarkiv.ferdigstilljournalpost.v1.util.Utils.validateId;
import static no.nav.dokarkiv.ferdigstilljournalpost.v1.util.Utils.validateJournalfEnhet;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.ferdigstilljournalpost.v1.api.FerdigstillJournalpostRequest;
import no.nav.dokarkiv.ferdigstilljournalpost.v1.ferdigstill.FerdigstillJournalpostService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@Slf4j
@RestController
@RequestMapping("/rest/ferdigstilljournalpost/")
@Api(value = "FerdigstillJournalpost RestController")
public class FerdigstillJournalpostRestController {

	private final FerdigstillJournalpostService ferdigstillJournalpostService;
	private final AbacSecurityService abacSecurityService;

	@Inject
	public FerdigstillJournalpostRestController(final FerdigstillJournalpostService ferdigstillJournalpostService,
												final AbacSecurityService abacSecurityService) {
		this.ferdigstillJournalpostService = ferdigstillJournalpostService;
		this.abacSecurityService = abacSecurityService;
	}

	@Transactional
	@PatchMapping
	@ApiOperation("Ferdigstill journalpost")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "ferdigstill"}, percentiles = {0.5, 0.95})
	@ResponseBody
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok"),
			@ApiResponse(code = 400, message = "Kan ikke ferdigstille"),
			@ApiResponse(code = 500, message = "Internal server error")})
	public ResponseEntity<String> ferdigstillJournalpost(@RequestBody FerdigstillJournalpostRequest request) {
		validateRequest(request);

		abacSecurityService.assertAccessToJournalpost(request.getJournalpostId());

		ferdigstillJournalpostService.ferdigstill(request.getJournalpostId(), request.getJournalfEnhet());

		return ResponseEntity.ok().body("Journalpost ferdigstilt");
	}

	private void validateRequest(FerdigstillJournalpostRequest request) {
		validateId(request.getJournalpostId(), "journalpostId");
		validateJournalfEnhet(request.getJournalfEnhet(), "journalfEnhet");
	}
}
