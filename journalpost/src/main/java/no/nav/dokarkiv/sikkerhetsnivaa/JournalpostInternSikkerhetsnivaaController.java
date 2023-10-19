package no.nav.dokarkiv.sikkerhetsnivaa;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.sikkerhetsnivaa.JournalpostInternSikkerhetsnivaaController.SIKKERHETSNIVAA_PATH;
import static no.nav.dokarkiv.sikkerhetsnivaa.JournalpostInternSikkerhetsnivaaController.SIKKERHETSNIVAA_ROLE;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@Tag(name = "journalpostapi - internt", description = "Interne tjenester mot journalpost for sikkerhetsnivaa")
@Slf4j
@RestController
@Transactional(readOnly = true)
@RequestMapping(SIKKERHETSNIVAA_PATH)
@ProtectedWithClaims(issuer = ISSUER_AZUREV2, claimMap = {"roles=" + SIKKERHETSNIVAA_ROLE})
public class JournalpostInternSikkerhetsnivaaController {

	public static final String SIKKERHETSNIVAA_ROLE = "api_intern_sikkerhetsnivaa";
	public static final String SIKKERHETSNIVAA_PATH = "/rest/internal/sikkerhetsnivaa";

	private final FinnUlesteJournalposterService finnUlesteJournalposterService;

	public JournalpostInternSikkerhetsnivaaController(FinnUlesteJournalposterService finnUlesteJournalposterService) {
		this.finnUlesteJournalposterService = finnUlesteJournalposterService;
	}

	@SwaggerFinnUlesteJournalposter
	@GetMapping("/finnUlesteJournalposter/{utsendingskanal}/{ekspedertFra}/{ekspedertTil}")
	public ResponseEntity<List<Long>> finnUlesteJournalposter(@PathVariable @NotBlank(message = "Sti-parameter utsendingskanal må ha en verdi") String utsendingskanal,
															  @PathVariable @NotBlank(message = "Sti-parameter ekspedertFra må ha en verdi") @DateTimeFormat(iso = DATE_TIME) LocalDateTime ekspedertFra,
															  @PathVariable @NotBlank(message = "Sti-parameter ekspedertTil må ha en verdi") @DateTimeFormat(iso = DATE_TIME) LocalDateTime ekspedertTil) {

		log.info(format("finnUlesteJournalposter har mottatt kall for å hente alle uleste journalposter i tidsrommet ekspedertFra=%s og ekspedertTil=%s med utsendingskanal=%s", ekspedertFra, ekspedertTil, utsendingskanal));

		List<Long> journalpostIder = finnUlesteJournalposterService.finnUlesteJournalposter(utsendingskanal, ekspedertFra, ekspedertTil);

		log.info(format("finnUlesteJournalposter fant %s uleste journalposter i tidsrommet ekspedertFra=%s, ekspedertTil=%s med utsendingskanal=%s", journalpostIder.size(), ekspedertFra, ekspedertTil, utsendingskanal));

		return ResponseEntity.ok().body(journalpostIder);
	}

}

