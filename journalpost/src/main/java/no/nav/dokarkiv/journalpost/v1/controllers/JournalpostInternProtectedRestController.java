package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.MottaDokumentUtgaaendeSkanningRequest;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.FinnMottatteJournalposterResponse;
import no.nav.dokarkiv.journalpost.v1.services.FinnIkkeLesteJournalposterService;
import no.nav.dokarkiv.journalpost.v1.services.FinnMottatteJournalposterService;
import no.nav.dokarkiv.journalpost.v1.services.MottaDokumentUtgaaendeSkanningService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFinnIkkeLesteJournalposter;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFinnMottatteJournalposterMedTemaEldreEnn;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerMottaDokumentUtgaaendeSkanning;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateId;

@Tag(name = "journalpostapi - internt", description = "Interne tjenester mot journalpost")
@Slf4j
@Protected
@RestController
@RequestMapping("/rest/internal/journalpostapi/v1")
public class JournalpostInternProtectedRestController {

	private final FinnMottatteJournalposterService finnMottatteJournalposterService;
	private final MottaDokumentUtgaaendeSkanningService mottaDokumentUtgaaendeSkanningService;
	private final FinnIkkeLesteJournalposterService finnIkkeLesteJournalposterService;

	public JournalpostInternProtectedRestController(FinnMottatteJournalposterService finnMottatteJournalposterService,
													MottaDokumentUtgaaendeSkanningService mottaDokumentUtgaaendeSkanningService,
													FinnIkkeLesteJournalposterService finnIkkeLesteJournalposterService) {
		this.finnMottatteJournalposterService = finnMottatteJournalposterService;
		this.mottaDokumentUtgaaendeSkanningService = mottaDokumentUtgaaendeSkanningService;
		this.finnIkkeLesteJournalposterService = finnIkkeLesteJournalposterService;
	}


	@Protected
	@ResponseBody
	@Transactional(readOnly = true)
	@SwaggerFinnMottatteJournalposterMedTemaEldreEnn
	@GetMapping(value = "/finnMottatteJournalposter/{temaer}/{eldreEnn}")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "finnMottatteJournalposter"}, percentiles = {0.5, 0.95})
	public ResponseEntity<FinnMottatteJournalposterResponse> finnMottatteJournalposterMedTemaEldreEnn(
			@PathVariable("temaer") List<String> temaer,
			@PathVariable("eldreEnn") int eldreEnn) {
		MDC.put(MDC_REQUEST_ID, "finnMottatteJournalposter");
		try {
			log.info("finnMottatteJournalposterMedTemaEldreEnn har mottatt kall om å hente ubehandlede journalposter med tema blandt={}", temaer);
			FinnMottatteJournalposterResponse ubehandledeJournalposter = finnMottatteJournalposterService.finnMottatteJournalposterMedTemaEldreEnn(temaer, eldreEnn);
			log.info("finnMottatteJournalposterMedTemaEldreEnn returnerer {} journalposter for tema={}", ubehandledeJournalposter.getJournalposter().size(), temaer);

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

	@Protected
	@Transactional
	@SwaggerMottaDokumentUtgaaendeSkanning
	@PutMapping("/journalpost/{journalpostId}/mottaDokumentUtgaaendeSkanning")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "mottaDokumentUtgaaendeSkanning"}, percentiles = {0.5, 0.95})
	public ResponseEntity<Long> mottaDokumentUtgaaendeSkanning(
			@PathVariable String journalpostId,
			@RequestBody MottaDokumentUtgaaendeSkanningRequest request) {
		try {
			RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
			MDC.put(MDC_REQUEST_ID, "mottaDokumentUtgaaendeSkanning");
			log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall med journalpostId={}", journalpostId);

			validateId(journalpostId, "journalpostId");

			mottaDokumentUtgaaendeSkanningService.mottaDokumentUtgaaendeSkanning(Long.parseLong(journalpostId), request);

			return ResponseEntity.ok().build();
		} catch (DokarkivFunctionalException e) {
			log.warn("mottaDokumentUtgaaendeSkanning - feilet funksjonelt ved mottak av utgaaende skanning for journalpostId={}. Feilmelding={}", journalpostId, e
					.getMessage());
			throw e;
		} catch (DokarkivTechnicalException e) {
			log.error("mottaDokumentUtgaaendeSkanning - feilet teknisk ved mottak av utgaaende skanning for journalpostId={}. Feilmelding={}", journalpostId, e
					.getMessage());
			throw e;
		}
	}

	@Transactional(readOnly = true)
	@SwaggerFinnIkkeLesteJournalposter
	@GetMapping("/finnIkkeLesteJournalposter/{utsendingsKanal}/{ekspedertFra}/{ekspedertTil}")
	public ResponseEntity<List<Long>> finnIkkeLesteJournalposter(@PathVariable String utsendingsKanal,
																 @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ekspedertFra,
																 @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ekspedertTil) {

		//Hvordan asserter vi at det er dokdistavstemming som har kalt appen?
		//Kan det utledes fra tokenet på noe vis?
		log.info(String.format("finnIkkeLesteJournalposter har mottatt kall for å hente alle uleste journalposter i tidsrommet ekspedertFra=%s og ekspedertTil=%s med utsendingskanal=%s", ekspedertFra, ekspedertTil, utsendingsKanal));
		List<Long> journalpostIds = finnIkkeLesteJournalposterService.finnIkkeLesteJournalposter(utsendingsKanal, ekspedertFra, ekspedertTil);
		log.info(String.format("finnIkkeLesteJournalposter fant %s uleste journalposter i tidsrommet ekspedertFra=%s, ekspedertTil=%s med utsendingskanal=%s", journalpostIds.size(), ekspedertFra, ekspedertTil, utsendingsKanal));
		return ResponseEntity.ok()
				.body(journalpostIds);
	}

}
