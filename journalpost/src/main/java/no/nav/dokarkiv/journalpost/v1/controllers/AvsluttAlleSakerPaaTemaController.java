package no.nav.dokarkiv.journalpost.v1.controllers;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.journalpost.v1.api.AvsluttAlleSakerPaaTemaRequest;
import no.nav.dokarkiv.journalpost.v1.services.AvsluttAlleSakerPaaTemaService;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.journalpost.v1.controllers.DokVaktmesterController.INTERN_ROLE;
import static no.nav.dokarkiv.journalpost.v1.validators.AvsluttAlleSakerPaaTemaValidator.validerAvsluttAlleSakerPaaTemaRequest;

@Slf4j
@RestController
@ProtectedWithClaims(issuer = ISSUER_AZUREV2, claimMap = {"roles=" + INTERN_ROLE})
@RequestMapping("/rest/internal/journalpostapi/v1")
public class AvsluttAlleSakerPaaTemaController {

	private final AvsluttAlleSakerPaaTemaService avsluttAlleSakerPaaTemaService;

	public AvsluttAlleSakerPaaTemaController(AvsluttAlleSakerPaaTemaService avsluttAlleSakerPaaTemaService) {
		this.avsluttAlleSakerPaaTemaService = avsluttAlleSakerPaaTemaService;
	}

	@PatchMapping("/avsluttAlleSakerPaaTema")
	public ResponseEntity<Void> avsluttAlleSakerPaaTema(@RequestBody AvsluttAlleSakerPaaTemaRequest request) {
		validerAvsluttAlleSakerPaaTemaRequest(request);
		log.info("avsluttAlleSakerPaaTema skal avslutte alle saker på tema={} med referanse={}, avsluttetDato={} og administrativEnhet={}",
				request.tema(), request.referanse(), request.avsluttetDato(), request.administrativEnhet());

		avsluttAlleSakerPaaTemaService.avsluttAlleSakerPaaTema(request);

		log.info("avsluttAlleSakerPaaTema har avsluttet alle saker på tema={} med referanse={}, avsluttetDato={} og administrativEnhet={}",
				request.tema(), request.referanse(), request.avsluttetDato(), request.administrativEnhet());

		return ResponseEntity.noContent().build();
	}

}