package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.journalpost.v1.services.FinnIkkeLesteJournalposterService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFinnIkkeLesteJournalposter;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AAD;
import static no.nav.dokarkiv.journalpost.v1.controllers.JournalpostInternSikkerhetsnivaaController.SIKKERHETSNIVAA_ROLE;

@Tag(name = "journalpostapi - internt", description = "Interne tjenester mot journalpost for sikkerhetsnivaa")
@Slf4j
@RestController
@RequestMapping("/rest/internal/journalpostapi/v1")
@ProtectedWithClaims(issuer = ISSUER_AAD, claimMap = {"roles=" + SIKKERHETSNIVAA_ROLE})
public class JournalpostInternSikkerhetsnivaaController {
	public static final String SIKKERHETSNIVAA_ROLE = "api_intern_sikkerhetsnivaa";

	private final FinnIkkeLesteJournalposterService finnIkkeLesteJournalposterService;

	public JournalpostInternSikkerhetsnivaaController(FinnIkkeLesteJournalposterService finnIkkeLesteJournalposterService) {
		this.finnIkkeLesteJournalposterService = finnIkkeLesteJournalposterService;
	}

	@Transactional(readOnly = true)
	@SwaggerFinnIkkeLesteJournalposter
	@GetMapping("/finnIkkeLesteJournalposter/{utsendingsKanal}/{ekspedertFra}/{ekspedertTil}")
	public ResponseEntity<List<Long>> finnIkkeLesteJournalposter(@PathVariable String utsendingsKanal,
																 @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ekspedertFra,
																 @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ekspedertTil) {
		log.info(String.format("finnIkkeLesteJournalposter har mottatt kall for å hente alle uleste journalposter i tidsrommet ekspedertFra=%s og ekspedertTil=%s med utsendingskanal=%s", ekspedertFra, ekspedertTil, utsendingsKanal));
		List<Long> journalpostIds = finnIkkeLesteJournalposterService.finnIkkeLesteJournalposter(utsendingsKanal, ekspedertFra, ekspedertTil);
		log.info(String.format("finnIkkeLesteJournalposter fant %s uleste journalposter i tidsrommet ekspedertFra=%s, ekspedertTil=%s med utsendingskanal=%s", journalpostIds.size(), ekspedertFra, ekspedertTil, utsendingsKanal));
		return ResponseEntity.ok()
				.body(journalpostIds);
	}

}

