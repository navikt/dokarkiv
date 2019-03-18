package no.nav.dokarkiv.journalpost.v1;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.CREATE_ACTION;
import static no.nav.dokarkiv.journalpost.v1.util.RequestUtils.validateJournalfoerendeEnhet;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.OpprettJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.rjoark201.FerdigstillJournalpostService;
import no.nav.dokarkiv.journalpost.v1.rjoark202.OpprettJournalpostService;
import no.nav.dokarkiv.journalpost.v1.rjoark202.util.OpprettJournalpostRequestValidator;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerOpprettJournalpost;
import no.nav.freg.abac.core.annotation.Abac;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Optional;

@Api
@Slf4j
@RestController
@RequestMapping("/rest/journalpostapi/v1/journalpost")
public class OpprettJournalpostRestController {

	private static final String TRUE = "true";

	private final OpprettJournalpostService service;
	private final OpprettJournalpostRequestValidator requestValidator;
	private final FerdigstillJournalpostService ferdigstillJournalpostService;

	@Inject
	public OpprettJournalpostRestController(final OpprettJournalpostService opprettJournalpostService,
											final FerdigstillJournalpostService ferdigstillJournalpostService) {
		this.service = opprettJournalpostService;
		this.requestValidator = new OpprettJournalpostRequestValidator();
		this.ferdigstillJournalpostService = ferdigstillJournalpostService;
	}

	@Transactional
	@PostMapping
	@SwaggerOpprettJournalpost
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = CREATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark202"}, percentiles = {0.5, 0.95}, histogram = true)
	public ResponseEntity<OpprettJournalpostResponse> opprettJournalpost(@RequestBody OpprettJournalpostRequest request,
																		 @RequestHeader(required = false) String aksjonsLoggHeader,
																		 @ApiParam(name = "ferdigstill", allowableValues = "true, false", required = false) @RequestParam(required = false) String ferdigstill) throws UgyldigAksjonsLoggException {
		MDC.put(MDC_REQUEST_ID, "rjoark202");
		log.info(MDC.get(MDC_REQUEST_ID) + " har mottat kall for opprettelse av ny journalpost");
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));

		// tilgangsstyring abac?

		requestValidator.validateRequest(request);

		Long journalpostId = service.opprettJournalpost(request, aksjonsLoggHeader);
		log.info(MDC.get(MDC_REQUEST_ID) + " har opprettet ny journalpost, journalpostId={}", journalpostId);

		Optional<Pair<String, String>> ferdigstillResponse = Optional.empty();
		if (TRUE.equals(ferdigstill)) {
			ferdigstillResponse = Optional.of(forsoekFerdigstill(journalpostId, request, aksjonsLoggHeader));
		}
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(OpprettJournalpostResponse.builder()
						.journalpostId(String.valueOf(journalpostId))
						.journalstatus(ferdigstillResponse.map(Pair::getKey).orElse(null))
						.melding(ferdigstillResponse.map(Pair::getValue).orElse(null))
						.build());
	}

	private Pair<String, String> forsoekFerdigstill(Long journalpostId, OpprettJournalpostRequest request, String aksjonsLoggHeader) throws UgyldigAksjonsLoggException {
		log.info(MDC.get(MDC_REQUEST_ID) + " forsøker å ferdigstille journalpost, journalpostId={}", journalpostId);
		try {
			validateJournalfoerendeEnhet(request.getJournalfoerendeEnhet(), "journalfoerendeEnhet");
			ferdigstillJournalpostService.ferdigstill(journalpostId, request.getJournalfoerendeEnhet(), aksjonsLoggHeader);
		} catch (DokarkivFunctionalException e) {
			return Pair.of("MIDLERTIDIG", e.getMessage());
		}
		log.info(MDC.get(MDC_REQUEST_ID) + " har ferdigstilt journalpost, journalpostId={}", journalpostId);
		return Pair.of("ENDELIG", null);
	}
}