package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.NavHeaders;
import no.nav.dokarkiv.core.exceptions.ConsumerIsNotSrvDokSikkerhetsnettFunctionalException;
import no.nav.dokarkiv.core.exceptions.ConsumerIsNotSrvDokarkivProxyFunctionalException;
import no.nav.dokarkiv.core.exceptions.ConsumerIsNotSrvSkanMotUtgaaendeFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.journalpost.v1.api.FeiledeDokumenter;
import no.nav.dokarkiv.journalpost.v1.api.MottaDokumentUtgaaendeSkanningRequest;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggResponse;
import no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.FinnMottatteJournalposterResponse;
import no.nav.dokarkiv.journalpost.v1.services.FinnMottatteJournalposterService;
import no.nav.dokarkiv.journalpost.v1.services.KopierJournalpostService;
import no.nav.dokarkiv.journalpost.v1.services.MottaDokumentUtgaaendeSkanningService;
import no.nav.dokarkiv.journalpost.v1.services.TilknyttVedleggService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFinnMottatteJournalposter;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFinnMottatteJournalposterMedTema;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerKopierJournalpost;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerMottaDokumentUtgaaendeSkanning;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerTilknyttVedlegg;
import no.nav.security.token.support.core.api.Unprotected;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
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
@Api(description = "Interne tjenester mot journalpost")
@Slf4j
@Unprotected
@RestController
@RequestMapping("/rest/intern/journalpostapi/v1")
public class JournalpostInternRestController {

	private final TilknyttVedleggService tilknyttVedleggService;
	private final FinnMottatteJournalposterService finnMottatteJournalposterService;
	private final KopierJournalpostService kopierJournalpostService;
	private final MottaDokumentUtgaaendeSkanningService mottaDokumentUtgaaendeSkanningService;
	private static final String SRVDOKARKIVPROXY = "srvdokarkivproxy";
	private static final String SRVDOKSIKKERHETSNETT = "srvdoksikkerhetsnt";
	private static final String SRVSKANMOTUTGAAENDE = "srvskanmotutgaaende";
	private static final int DEFAULT_DAGER_GAMLE = 5;

	@Inject
	public JournalpostInternRestController(
			final TilknyttVedleggService tilknyttVedleggService,
			final FinnMottatteJournalposterService finnMottatteJournalposterService,
			final KopierJournalpostService kopierJournalpostService,
			final MottaDokumentUtgaaendeSkanningService mottaDokumentUtgaaendeSkanningService
	) {
		this.tilknyttVedleggService = tilknyttVedleggService;
		this.finnMottatteJournalposterService = finnMottatteJournalposterService;
		this.kopierJournalpostService = kopierJournalpostService;
		this.mottaDokumentUtgaaendeSkanningService = mottaDokumentUtgaaendeSkanningService;
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

	@Transactional(readOnly = true)
	@SwaggerFinnMottatteJournalposterMedTema
	@ResponseBody
	@GetMapping(value = "/finnMottatteJournalposter/{temaer}")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "finnMottatteJournalposter"}, percentiles = {0.5, 0.95})
	public ResponseEntity<FinnMottatteJournalposterResponse> finnMottatteJournalposterMedTema(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION) String auth,
			@PathVariable List<String> temaer) {
		MDC.put(MDC_REQUEST_ID, "finnMottatteJournalposter");
		try {
			assertThatConsumerIsSrvdoksikkerhetsnett(auth);

			log.info("finnMottatteJournalposter har mottatt kall om å hente ubehandlede journalposter med tema blandt " + temaer);

			FinnMottatteJournalposterResponse ubehandledeJournalposter = finnMottatteJournalposterService.finnMottatteJournalposterMedTemaEldreEnn(temaer, DEFAULT_DAGER_GAMLE);

			ResponseEntity<FinnMottatteJournalposterResponse> re = ResponseEntity
					.ok()
					.body(ubehandledeJournalposter);
			return re;
		} catch (DokarkivFunctionalException e) {
			log.warn("finnMottatteJournalposter - feilet funksjonelt ved søk på ubehandlede journalposter med tema blandt {}. Feilmelding={}", temaer, e
					.getMessage());
			throw e;
		} catch (DokarkivTechnicalException e) {
			log.error("finnMottatteJournalposter - feilet teknisk ved søk på ubehandlede journalposter med tema blandt {}. Feilmelding={}", temaer, e
					.getMessage());
			throw e;
		}
	}

	@Transactional(readOnly = true)
	@SwaggerFinnMottatteJournalposter
	@ResponseBody
	@GetMapping(value = "/finnMottatteJournalposter")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "finnMottatteJournalposter"}, percentiles = {0.5, 0.95})
	public ResponseEntity<FinnMottatteJournalposterResponse> finnMottatteJournalposter(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION) String auth) {
		MDC.put(MDC_REQUEST_ID, "finnMottatteJournalposter");
		try {
			assertThatConsumerIsSrvdoksikkerhetsnett(auth);

			log.info("finnMottatteJournalposter har mottatt kall om å hente ubehandlede journalposter");

			FinnMottatteJournalposterResponse ubehandledeJournalposter = finnMottatteJournalposterService.finnMottatteJournalposter();

			return ResponseEntity
					.ok()
					.body(ubehandledeJournalposter);
		} catch (DokarkivFunctionalException e) {
			log.warn("tilknyttVedlegg - feilet funksjonelt ved søk på ubehandlede journalposter. Feilmelding={}", e
					.getMessage());
			throw e;
		} catch (DokarkivTechnicalException e) {
			log.error("tilknyttVedlegg - feilet teknisk ved søk på ubehandlede journalposter. Feilmelding={}", e
					.getMessage());
			throw e;
		}
	}

	@Transactional(readOnly = true)
	@ResponseBody
	@GetMapping(value = "/finnMottatteJournalposter/{temaer}/{eldreEnn}")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "finnMottatteJournalposter"}, percentiles = {0.5, 0.95})
	public ResponseEntity<FinnMottatteJournalposterResponse> finnMottatteJournalposterMedTema(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION) String auth,
			@PathVariable("temaer") List<String> temaer,
			@PathVariable("eldreEnn") int eldreEnn) {
		MDC.put(MDC_REQUEST_ID, "finnMottatteJournalposter");
		try {
			assertThatConsumerIsSrvdoksikkerhetsnett(auth);

			log.info("finnMottatteJournalposter_eldreEnn har mottatt kall om å hente ubehandlede journalposter med tema blandt " + temaer);

			FinnMottatteJournalposterResponse ubehandledeJournalposter = finnMottatteJournalposterService.finnMottatteJournalposterMedTemaEldreEnn(temaer, eldreEnn);

			ResponseEntity<FinnMottatteJournalposterResponse> re = ResponseEntity
					.ok()
					.body(ubehandledeJournalposter);
			return re;
		} catch (DokarkivFunctionalException e) {
			log.warn("finnMottatteJournalposter - feilet funksjonelt ved søk på ubehandlede journalposter med tema blandt {}. Feilmelding={}", temaer, e
					.getMessage());
			throw e;
		} catch (DokarkivTechnicalException e) {
			log.error("finnMottatteJournalposter - feilet teknisk ved søk på ubehandlede journalposter med tema blandt {}. Feilmelding={}", temaer, e
					.getMessage());
			throw e;
		}
	}

	@Transactional
	@SwaggerKopierJournalpost
	@PostMapping("/journalpost/kopierJournalpost")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark203"}, percentiles = {0.5, 0.95})
	public ResponseEntity<Long> kopierJournalpost(
			@io.swagger.annotations.ApiParam(name = "kildeJournalpostId", value = "IDen til journalposten som skal kopieres", required = true, example = "77778888")
			@RequestHeader(value = HttpHeaders.AUTHORIZATION) String auth,
			@RequestHeader(value = NavHeaders.NAV_USER_ID) String userId,
			@RequestParam String kildeJournalpostId) {
		try {
			assertThatConsumerIsSrvdokarkivproxy(auth);

			MDC.put(MDC_REQUEST_ID, "rjoark203");
			log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall for kopiering av journalpost med journalpostId={}", kildeJournalpostId);
			validateId(kildeJournalpostId, "journalpostId");

			Long nyJournalpostId = kopierJournalpostService.execute(Long.parseLong(kildeJournalpostId));

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

	@Transactional
	@SwaggerMottaDokumentUtgaaendeSkanning
	@PutMapping("/journalpost/{journalpostId}/mottaDokumentUtgaaendeSkanning")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "mottaDokumentUtgaaendeSkanning"}, percentiles = {0.5, 0.95})
	public ResponseEntity<Long> mottaDokumentUtgaaendeSkanning(
			@PathVariable String journalpostId,
			@RequestHeader(value = HttpHeaders.AUTHORIZATION) String auth,
			@RequestBody MottaDokumentUtgaaendeSkanningRequest request) {
		try {
			assertThatConsumerIsSrvskanmotutgaaende(auth);

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

	private void assertThatConsumerIsSrvdokarkivproxy(String auth) {
		if (!SRVDOKARKIVPROXY.equals(decodeBasicAuth(auth)[0])) {
			throw new ConsumerIsNotSrvDokarkivProxyFunctionalException("Konsument har ikke tilgang til å kalle tjenesten");
		}
	}

	private void assertThatConsumerIsSrvdoksikkerhetsnett(String auth) {
		if (!SRVDOKSIKKERHETSNETT.equals(decodeBasicAuth(auth)[0])) {
			throw new ConsumerIsNotSrvDokSikkerhetsnettFunctionalException("Konsument har ikke tilgang til å kalle tjenesten");
		}
	}

	private void assertThatConsumerIsSrvskanmotutgaaende(String auth) {
		if (!SRVSKANMOTUTGAAENDE.equals(decodeBasicAuth(auth)[0])) {
			throw new ConsumerIsNotSrvSkanMotUtgaaendeFunctionalException("Konsument har ikke tilgang til å kalle tjenesten");
		}
	}

}
