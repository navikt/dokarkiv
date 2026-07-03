package no.nav.dokarkiv.internal.finnulestejournalposter;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.internal.finnulestejournalposter.FinnUlesteJournalposterController.SIKKERHETSNIVAA_ROLE;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@Slf4j
@Tag(name = "journalpostapi - internt", description = "Interne tjenester mot journalpost for dokdistavstemming")
@RestController
@RequestMapping("/rest/internal")
@ProtectedWithClaims(issuer = ISSUER_AZUREV2, claimMap = {"roles=" + SIKKERHETSNIVAA_ROLE})
public class FinnUlesteJournalposterController {

	public static final String SIKKERHETSNIVAA_ROLE = "api_intern_sikkerhetsnivaa";

	private final FinnUlesteJournalposterService finnUlesteJournalposterService;

	public FinnUlesteJournalposterController(FinnUlesteJournalposterService finnUlesteJournalposterService) {
		this.finnUlesteJournalposterService = finnUlesteJournalposterService;
	}

	@SwaggerFinnUlesteJournalposter
	@GetMapping("/finnUlesteJournalposter/{utsendingskanal}/{ekspedertFra}/{ekspedertTil}")
	public ResponseEntity<List<Long>> finnUlesteJournalposter(@PathVariable @NotBlank(message = "Sti-parameter utsendingskanal må ha en verdi") String utsendingskanal,
															  @PathVariable @NotNull(message = "Sti-parameter ekspedertFra må ha en verdi") @DateTimeFormat(iso = DATE_TIME) LocalDateTime ekspedertFra,
															  @PathVariable @NotNull(message = "Sti-parameter ekspedertTil må ha en verdi") @DateTimeFormat(iso = DATE_TIME) LocalDateTime ekspedertTil) {
		log.info(format("finnUlesteJournalposter har mottatt kall for å hente alle uleste journalposter i tidsrommet ekspedertFra=%s og ekspedertTil=%s med utsendingskanal=%s", ekspedertFra, ekspedertTil, utsendingskanal));

		List<Long> journalpostIder = finnUlesteJournalposterService.finnUlesteJournalposter(utsendingskanal, ekspedertFra, ekspedertTil);

		log.info(format("finnUlesteJournalposter fant %s uleste journalposter i tidsrommet ekspedertFra=%s, ekspedertTil=%s med utsendingskanal=%s", journalpostIder.size(), ekspedertFra, ekspedertTil, utsendingskanal));

		return ResponseEntity.ok().body(journalpostIder);
	}
}
