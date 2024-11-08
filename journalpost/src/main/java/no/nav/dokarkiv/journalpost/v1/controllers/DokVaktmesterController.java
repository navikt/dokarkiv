package no.nav.dokarkiv.journalpost.v1.controllers;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigJournalStatusException;
import no.nav.dokarkiv.journalpost.v1.services.SettAvbruttJournalpostTilRedigeringService;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.core.stelvio.RequestContextUtil.createAndSetUsername;
import static no.nav.dokarkiv.journalpost.v1.controllers.DokVaktmesterController.INTERN_ROLE;

@Slf4j
@RestController
@RequestMapping("/rest/internal/journalpostapi/v1")
@ProtectedWithClaims(issuer = ISSUER_AZUREV2, claimMap = {"roles=" + INTERN_ROLE})
public class DokVaktmesterController {
	public static final String INTERN_ROLE = "api_intern";
	public static final String AVBRUTT_JOURNALPOST_SATT_TIL_REDIGERBAR_MESSAGE = "Journalpost tilbakestilt til redigerbar tilstand";

	private final SettAvbruttJournalpostTilRedigeringService settAvbruttJournalpostTilRedigeringService;

	public DokVaktmesterController(SettAvbruttJournalpostTilRedigeringService settAvbruttJournalpostTilRedigeringService) {
		this.settAvbruttJournalpostTilRedigeringService = settAvbruttJournalpostTilRedigeringService;
	}

	@PutMapping("/journalpost/{journalpostId}/settAvbruttJournalpostRedigerbar")
	public ResponseEntity<String> settAvbruttJournalpostTilRedigering(
			@PathVariable Long journalpostId
	) {
		MDC.put(MDC_REQUEST_ID, "settAvbruttJournalpostRedigerbar");
		createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));

		try {
			log.info("{} har mottatt kall for å sette journalpost med journalpostId={} redigerbar", MDC.get(MDC_REQUEST_ID), journalpostId);

			settAvbruttJournalpostTilRedigeringService.settAvbruttJournalpostTilRedigering(journalpostId);

			log.info("{} har sattt journalpost med journalpostId={} redigerbar", MDC.get(MDC_REQUEST_ID), journalpostId);
			return ResponseEntity.ok().body(AVBRUTT_JOURNALPOST_SATT_TIL_REDIGERBAR_MESSAGE);
		} catch (JournalpostIkkeFunnetException e) {
			log.warn("SettAvbruttJournalpostTilRedigering fant ikke journalpost med journalpostId={}. Feilmelding={}", journalpostId, e.getMessage());
			return ResponseEntity
					.badRequest()
					.body(String.format("Journalpost med journalpostId=%s ble ikke funnet, eller mangler Hoveddokumentrelasjon.", journalpostId));
		} catch (UgyldigJournalStatusException e) {
			log.warn("SettAvbruttJournalpostTilRedigering kan ikke endre status for journalpost med journalpostId={}. Feilmelding={}", journalpostId, e.getMessage());
			return ResponseEntity
					.status(HttpStatus.CONFLICT)
					.body(String.format("Kan ikke endre status for journalpost med journalpostId=%s. Journalposten har feil status", journalpostId));
		}
	}
}
