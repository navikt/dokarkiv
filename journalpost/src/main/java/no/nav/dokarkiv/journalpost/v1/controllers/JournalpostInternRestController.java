package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.NavHeaders;
import no.nav.dokarkiv.core.exceptions.ConsumerIsNotSrvDokarkivProxyFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.journalpost.v1.api.FeiledeDokumenter;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggResponse;
import no.nav.dokarkiv.journalpost.v1.services.KopierJournalpostService;
import no.nav.dokarkiv.journalpost.v1.services.MottaDokumentUtgaaendeSkanningService;
import no.nav.dokarkiv.journalpost.v1.services.TilknyttVedleggService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerKopierJournalpost;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerTilknyttVedlegg;
import no.nav.security.token.support.core.api.Unprotected;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.util.DecodeUtils.decodeBasicAuth;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateId;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
@Tag(name="journalpostapi - internt", description = "Interne tjenester mot journalpost")
@Slf4j
@Unprotected
@RestController
@RequestMapping("/rest/intern/journalpostapi/v1")
public class JournalpostInternRestController {

	private final TilknyttVedleggService tilknyttVedleggService;
	private final KopierJournalpostService kopierJournalpostService;
	private static final String SRVDOKARKIVPROXY = "srvdokarkivproxy";

	@Inject
	public JournalpostInternRestController(
			final TilknyttVedleggService tilknyttVedleggService,
			final KopierJournalpostService kopierJournalpostService) {
		this.tilknyttVedleggService = tilknyttVedleggService;
		this.kopierJournalpostService = kopierJournalpostService;
	}

	@Transactional
	@SwaggerTilknyttVedlegg
	@ResponseBody
	@PutMapping(value = "/journalpost/{journalpostId}/tilknyttVedlegg")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "tilknyttVedlegg"}, percentiles = {0.5, 0.95})
	public ResponseEntity<TilknyttVedleggResponse> tilknyttVedlegg(
			@PathVariable String journalpostId,
			@RequestHeader(value = HttpHeaders.AUTHORIZATION) String auth,
			@RequestBody TilknyttVedleggRequest request) {
		MDC.put(MDC_REQUEST_ID, "tilknyttVedlegg");
		try {
			assertThatConsumerIsSrvdokarkivproxy(auth);

			validateId(journalpostId, "journalpostId");

			log.info("tilknyttVedlegg har mottatt kall om å legge til vedlegg på journalpostId={}", journalpostId);

			List<FeiledeDokumenter> feiledeDokumenterList = tilknyttVedleggService.tilknyttVedlegg(Long.parseLong(journalpostId), request);

			if (feiledeDokumenterList.isEmpty()) {
				return ResponseEntity
						.ok()
						.body(TilknyttVedleggResponse.builder().build());
			} else {
				return ResponseEntity
						.status(HttpStatus.MULTI_STATUS)
						.body(TilknyttVedleggResponse.builder().feiledeDokumenter(feiledeDokumenterList).build());
			}

		} catch (DokarkivFunctionalException e) {
			log.warn("tilknyttVedlegg - feilet funksjonelt ved tilknytning av vedlegg for journalpostId={}. Feilmelding={}", journalpostId, e
					.getMessage());
			throw e;
		} catch (DokarkivTechnicalException e) {
			log.error("tilknyttVedlegg - feilet teknisk ved tilknytning av vedlegg for journalpostId={}. Feilmelding={}", journalpostId, e
					.getMessage());
			throw e;
		}
	}

	@Transactional
	@SwaggerKopierJournalpost
	@PostMapping("/journalpost/kopierJournalpost")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark203"}, percentiles = {0.5, 0.95})
	public ResponseEntity<Long> kopierJournalpost(
			@Parameter(name = "kildeJournalpostId", description = "IDen til journalposten som skal kopieres", required = true, example = "77778888")
			@RequestHeader(value = HttpHeaders.AUTHORIZATION) String auth,
			@RequestHeader(value = NavHeaders.NAV_USER_ID) String userId,
			@RequestParam String kildeJournalpostId) {
		try {
			assertThatConsumerIsSrvdokarkivproxy(auth);

			MDC.put(MDC_REQUEST_ID, "rjoark203");
			log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall for kopiering av journalpost med journalpostId={}", kildeJournalpostId);
			validateId(kildeJournalpostId, "journalpostId");

			Long nyJournalpostId = kopierJournalpostService.kopierJournalpost(Long.parseLong(kildeJournalpostId));

			return ResponseEntity.status(HttpStatus.CREATED).body(nyJournalpostId);
		} catch (DokarkivFunctionalException e) {
			log.warn("kopierJournalpost - feilet funksjonelt ved kopiering av journalpost for journalpostId={}. Feilmelding={}", kildeJournalpostId, e
					.getMessage());
			throw e;
		} catch (DokarkivTechnicalException e) {
			log.error("kopierJournalpost - feilet teknisk ved kopiering av journalpost for journalpostId={}. Feilmelding={}", kildeJournalpostId, e
					.getMessage());
			throw e;
		}
	}

	private void assertThatConsumerIsSrvdokarkivproxy(String auth) {
		if (!SRVDOKARKIVPROXY.equals(decodeBasicAuth(auth)[0])) {
			throw new ConsumerIsNotSrvDokarkivProxyFunctionalException("Konsument har ikke tilgang til å kalle tjenesten");
		}
	}

}
