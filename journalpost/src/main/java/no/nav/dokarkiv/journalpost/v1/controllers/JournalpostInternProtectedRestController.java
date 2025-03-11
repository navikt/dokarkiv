package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.AvsluttAlleSakerPaaTemaRequest;
import no.nav.dokarkiv.journalpost.v1.api.MottaDokumentUtgaaendeSkanningRequest;
import no.nav.dokarkiv.journalpost.v1.services.AvsluttAlleSakerPaaTemaService;
import no.nav.dokarkiv.journalpost.v1.services.MottaDokumentUtgaaendeSkanningService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerAvsluttAlleSakerPaaTema;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerMottaDokumentUtgaaendeSkanning;
import no.nav.security.token.support.core.api.Protected;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.journalpost.v1.controllers.DokVaktmesterController.INTERN_ROLE;
import static no.nav.dokarkiv.journalpost.v1.validators.AvsluttAlleSakerPaaTemaValidator.validerAvsluttAlleSakerPaaTemaRequest;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateIdAndParse;

@Slf4j
@Protected
@RestController
@RequestMapping("/rest/internal/journalpostapi/v1")
@Tag(name = "journalpostapi - internt", description = "Interne tjenester mot journalpost eller sak")
public class JournalpostInternProtectedRestController {

	private final AvsluttAlleSakerPaaTemaService avsluttAlleSakerPaaTemaService;
	private final MottaDokumentUtgaaendeSkanningService mottaDokumentUtgaaendeSkanningService;

	public JournalpostInternProtectedRestController(AvsluttAlleSakerPaaTemaService avsluttAlleSakerPaaTemaService, MottaDokumentUtgaaendeSkanningService mottaDokumentUtgaaendeSkanningService) {
		this.avsluttAlleSakerPaaTemaService = avsluttAlleSakerPaaTemaService;
		this.mottaDokumentUtgaaendeSkanningService = mottaDokumentUtgaaendeSkanningService;
	}

	@ProtectedWithClaims(issuer = ISSUER_AZUREV2, claimMap = {"roles=" + INTERN_ROLE})
	@PatchMapping("/avsluttAlleSakerPaaTema")
	@SwaggerAvsluttAlleSakerPaaTema
	public ResponseEntity<Void> avsluttAlleSakerPaaTema(@RequestBody AvsluttAlleSakerPaaTemaRequest request) {
		validerAvsluttAlleSakerPaaTemaRequest(request);
		log.info("avsluttAlleSakerPaaTema skal avslutte alle saker på tema={} med referanse={}, avsluttetDato={} og administrativEnhet={}",
				request.tema(), request.referanse(), request.avsluttetDato(), request.administrativEnhet());

		avsluttAlleSakerPaaTemaService.avsluttAlleSakerPaaTema(request);

		log.info("avsluttAlleSakerPaaTema har avsluttet alle saker på tema={} med referanse={}, avsluttetDato={} og administrativEnhet={}",
				request.tema(), request.referanse(), request.avsluttetDato(), request.administrativEnhet());

		return ResponseEntity.noContent().build();
	}

	@Transactional
	@Protected
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