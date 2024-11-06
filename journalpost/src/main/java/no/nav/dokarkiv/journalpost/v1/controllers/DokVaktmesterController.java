package no.nav.dokarkiv.journalpost.v1.controllers;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigJournalStatusException;
import no.nav.dokarkiv.journalpost.v1.services.SettAvbruttJournalpostTilRedigeringService;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.journalpost.v1.controllers.DokVaktmesterController.INTERN_ROLE;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateIdAndParse;

@Slf4j
@RestController
@RequestMapping("/rest/internal/journalpostapi/v1")
@ProtectedWithClaims(issuer = ISSUER_AZUREV2, claimMap = {"roles=" + INTERN_ROLE})
public class DokVaktmesterController {
	private final SettAvbruttJournalpostTilRedigeringService settAvbruttJournalpostTilRedigeringService;
	public static final String INTERN_ROLE = "api_intern";
	public static final String AVBRUTT_JOURNALPOST_SATT_TIL_REDIGERBAR_MESSAGE = "Journalpost tilbakestilt til redigerbar tilstand";

	public DokVaktmesterController(SettAvbruttJournalpostTilRedigeringService settAvbruttJournalpostTilRedigeringService){
		this.settAvbruttJournalpostTilRedigeringService = settAvbruttJournalpostTilRedigeringService;
	}

	@Transactional
	@PutMapping("/journalpost/{journalpostId}/settAvbruttJournalpostTilRedigering")
	public ResponseEntity<String> settAvbruttJournalpostTilRedigering(
			@PathVariable String journalpostId
	) {
		MDC.put(MDC_REQUEST_ID, "Avbrutt"); //TODO: bedre navn
		long journalpostIdParsed = validateIdAndParse(journalpostId, "JournalpostId");
		log.info("{} har mottatt kall for oppdatering av distribusjonsinfo for journalpostId={}", MDC.get(MDC_REQUEST_ID), journalpostIdParsed);
		try {
			settAvbruttJournalpostTilRedigeringService.settAvbruttJournalpostTilRedigering(journalpostIdParsed);

			return ResponseEntity.ok().body(AVBRUTT_JOURNALPOST_SATT_TIL_REDIGERBAR_MESSAGE);
		}
		catch (JournalpostIkkeFunnetException e){
			log.warn("SettAvbruttJournalpostTilRedigering fant ikke journalpost med journalpostId={}. Feilmelding={}", journalpostIdParsed, e.getMessage() );
			return ResponseEntity.notFound().build();
		}
		catch (UgyldigJournalStatusException e){
			log.warn("SettAvbruttJournalpostTilRedigering kan ikke endre status for journalpost med journalpostId={}. Feilmelding={}", journalpostIdParsed, e.getMessage());
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}


	}
}
