package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.DokumentUnderRedigeringException;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.InvalidPdfException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeMidlertidigException;
import no.nav.dokarkiv.core.exceptions.KanIkkeFerdigstilleException;
import no.nav.dokarkiv.core.exceptions.KanIkkeKopiereException;
import no.nav.dokarkiv.core.exceptions.KanIkkeLeggeTilVedleggException;
import no.nav.dokarkiv.core.exceptions.KanIkkeOppdatereDistribusjonsinfoException;
import no.nav.dokarkiv.core.exceptions.KanIkkeSlettetVedleggKnyttetTilJournalpostException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.FerdigstillJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.FjernVedleggTilknyttetJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.KopierJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.KopierJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterDistribusjonsinfoRequest;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.api.lastOppVedlegg.LastOppVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.lastOppVedlegg.LastOppVedleggResponse;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.DokumentInfoId;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostResult;
import no.nav.dokarkiv.journalpost.v1.services.FerdigstillJournalpostService;
import no.nav.dokarkiv.journalpost.v1.services.FjernVedleggTilknyttetJournalpost;
import no.nav.dokarkiv.journalpost.v1.services.KopierJournalpostResult;
import no.nav.dokarkiv.journalpost.v1.services.KopierJournalpostService;
import no.nav.dokarkiv.journalpost.v1.services.LastOppVedleggService;
import no.nav.dokarkiv.journalpost.v1.services.OppdaterDistribusjonsinfoService;
import no.nav.dokarkiv.journalpost.v1.services.OppdaterJournalpostService;
import no.nav.dokarkiv.journalpost.v1.services.OpprettJournalpostService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFerdigstillJournalpost;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFjernVedlegg;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerKopierJournalpost;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerLastOppVedlegg;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerOppdaterDistribusjonsinfo;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerOppdaterJournalpost;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerOpprettJournalpost;
import no.nav.dokarkiv.journalpost.v1.validators.FerdigstillJournalpostValidator;
import no.nav.dokarkiv.journalpost.v1.validators.LastOppVedleggValidator;
import no.nav.dokarkiv.journalpost.v1.validators.OppdaterDistribusjonsinfoValidator;
import no.nav.dokarkiv.journalpost.v1.validators.OpprettJournalpostRequestValidator;
import no.nav.security.token.support.core.api.Protected;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_JOURNALPOST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateEksternReferanseId;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateIdAndParse;
import static no.nav.dokarkiv.journalpost.v1.validators.OpprettJournalpostRequestValidator.MASKINELL_JOURNALFOERENDE_ENHET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Tag(name = "journalpostapi", description = "Tjenester for å arkivere og journalføre i fagarkiv")
@Slf4j
@Protected
@RestController
@RequestMapping("/rest/journalpostapi/v1/journalpost")
public class ArkiverOgJournalfoerRestController {

	private static final String TRUE = "true";
	private static final String MIDLERTIDIG = "MIDLERTIDIG";
	private static final String STATUS_ENDELIG = "ENDELIG";
	private final FerdigstillJournalpostService ferdigstillJournalpostService;
	private final OppdaterJournalpostService oppdaterJournalpostService;
	private final OppdaterDistribusjonsinfoService oppdaterDistribusjonsinfoService;
	private final OpprettJournalpostService opprettJournalpostService;
	private final OpprettJournalpostRequestValidator opprettJournalpostRequestValidator;
	private final FerdigstillJournalpostValidator ferdigstillJournalpostValidator;
	private final FjernVedleggTilknyttetJournalpost fjernVedleggTilknyttJournalpost;
	private final KopierJournalpostService kopierJournalpostService;
	private final LastOppVedleggService lastOppVedleggService;

	public ArkiverOgJournalfoerRestController(final FerdigstillJournalpostService ferdigstillJournalpostService,
											  final OppdaterJournalpostService oppdaterJournalpostService,
											  final OpprettJournalpostService opprettJournalpostService,
											  final OppdaterDistribusjonsinfoService oppdaterDistribusjonsinfoService,
											  final FjernVedleggTilknyttetJournalpost fjernVedleggTilknyttJournalpost,
											  final KopierJournalpostService kopierJournalpostService,
											  final LastOppVedleggService lastOppVedleggService) {
		this.ferdigstillJournalpostService = ferdigstillJournalpostService;
		this.oppdaterJournalpostService = oppdaterJournalpostService;
		this.opprettJournalpostService = opprettJournalpostService;
		this.fjernVedleggTilknyttJournalpost = fjernVedleggTilknyttJournalpost;
		this.oppdaterDistribusjonsinfoService = oppdaterDistribusjonsinfoService;
		this.opprettJournalpostRequestValidator = new OpprettJournalpostRequestValidator();
		this.ferdigstillJournalpostValidator = new FerdigstillJournalpostValidator();
		this.kopierJournalpostService = kopierJournalpostService;
		this.lastOppVedleggService = lastOppVedleggService;
	}

	@Transactional
	@SwaggerFerdigstillJournalpost
	@PatchMapping(value = "/{journalpostId}/ferdigstill")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark201"}, percentiles = {0.5, 0.95})
	public ResponseEntity<String> ferdigstillJournalpost(
			@PathVariable @Parameter(description = "IDen til journalposten som skal ferdigstilles", required = true, example = "77778888") String journalpostId,
			@RequestBody FerdigstillJournalpostRequest request
	) {
		MDC.put(MDC_REQUEST_ID, "rjoark201");
		long journalpostIdParsed = validateIdAndParse(journalpostId, "journalpostId");
		log.info("{} har mottatt kall for ferdigstilling av journalpost med journalpostId={}", MDC.get(MDC_REQUEST_ID), journalpostIdParsed);
		MDC.put(MDC_JOURNALPOST_ID, String.valueOf(journalpostIdParsed));

		try {
			ferdigstillJournalpostValidator.validateRequest(request);
			RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

			ferdigstillJournalpostService.ferdigstill(journalpostIdParsed, request);
			log.info("{} har ferdigstilt journalpost med journalpostId={}", MDC.get(MDC_REQUEST_ID), journalpostIdParsed);

			return ResponseEntity.ok()
					.contentType(APPLICATION_JSON)
					.body("\"Journalpost ferdigstilt\"");

		} catch (KanIkkeFerdigstilleException | JournalpostIkkeMidlertidigException |
				 DokumentUnderRedigeringException e) {
			throw new ResponseStatusException(BAD_REQUEST,
					format("Kunne ikke ferdigstille journalpost med journalpostId=%s. %s", journalpostIdParsed, e.getMessage()));
		}
	}

	@Transactional
	@SwaggerOppdaterDistribusjonsinfo
	@PatchMapping("/{journalpostId}/oppdaterDistribusjonsinfo")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark201"}, percentiles = {0.5, 0.95})
	public ResponseEntity<String> oppdaterDistribusjonsinfo(
			@PathVariable @Parameter(description = "IDen til journalposten som skal oppdateres", required = true, example = "77778888") String journalpostId,
			@RequestBody OppdaterDistribusjonsinfoRequest request) {
		MDC.put(MDC_REQUEST_ID, "oppdaterDistribusjonsinfo");
		long journalpostIdParsed = validateIdAndParse(journalpostId, "journalpostId");
		log.info("{} har mottatt kall for oppdatering av distribusjonsinfo for journalpostId={}", MDC.get(MDC_REQUEST_ID), journalpostIdParsed);
		MDC.put(MDC_JOURNALPOST_ID, String.valueOf(journalpostIdParsed));

		try {
			OppdaterDistribusjonsinfoValidator.validateRequest(request);
			RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

			oppdaterDistribusjonsinfoService.oppdaterDistribusjonsinfo(journalpostIdParsed, request);

			log.info("{} har oppdatert distribusjonsinfo på journalpost med journalpostId={}", MDC.get(MDC_REQUEST_ID), journalpostIdParsed);

			return ResponseEntity.ok()
					.contentType(APPLICATION_JSON)
					.body("\"Journalpost oppdatert\"");

		} catch (InputValideringFeiletException | KanIkkeOppdatereDistribusjonsinfoException e) {
			throw new ResponseStatusException(BAD_REQUEST,
					format("Kunne ikke oppdatere distribusjonsinfo for journalpost med journalpostId=%s. %s", journalpostIdParsed, e.getMessage()));
		}
	}

	@Transactional
	@SwaggerOppdaterJournalpost
	@ResponseBody
	@PutMapping(value = "/{journalpostId}")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "oppdaterjournalpost"}, percentiles = {0.5, 0.95})
	public OppdaterJournalpostResponse oppdaterJournalpost(
			@Parameter(
					name = "journalpostId",
					description = "Angir JournalpostId som skal oppdatere f.eks. 467011764",
					required = true,
					example = "467011764"
			)
			@PathVariable String journalpostId,
			@RequestBody OppdaterJournalpostRequest request) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		MDC.put(MDC_REQUEST_ID, "oppdaterjournalpost");
		long journalpostIdParsed = validateIdAndParse(journalpostId, "journalpostId");
		MDC.put(MDC_JOURNALPOST_ID, String.valueOf(journalpostIdParsed));
		log.info("{} har mottatt kall om å oppdatere journalpost med journalpostId={}", MDC.get(MDC_REQUEST_ID), journalpostIdParsed);

		try {
			oppdaterJournalpostService.oppdaterJournalpost(journalpostIdParsed, request);
			log.info("oppdaterjournalpost har oppdatert journalpost med journalpostId={} i Joark.", journalpostIdParsed);

			return OppdaterJournalpostResponse.builder().journalpostId(String.valueOf(journalpostIdParsed)).build();
		} catch (InputValideringFeiletException e) {
			throw new ResponseStatusException(BAD_REQUEST,
					format("Kunne ikke oppdatere journalpost med journalpostId=%s. %s", journalpostIdParsed, e.getMessage()));
		}
	}

	@Transactional
	@PostMapping
	@SwaggerOpprettJournalpost
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark202"}, percentiles = {0.5, 0.95}, histogram = true)
	public ResponseEntity<OpprettJournalpostResponse> opprettJournalpost(
			@RequestBody OpprettJournalpostRequest request,
			@Parameter(
					name = "forsoekFerdigstill",
					description = """
							Angir hvorvidt tjenesten skal forsøke å ferdigstille eller ikke. Når journalposten ferdigstilles, blir den låst for senere endringer.
							
							Dersom ferdigstilling ikke lykkes, returnerer tjenesten journalpostFerdigstilt=false
							
							Journalposten blir opprettet i alle tilfeller, men kan bare ferdigstilles dersom (minst) følgende er satt på input:
							* bruker
							* sak
							* tema
							* kanal (for inngående journalposter)
							* journalfoerendeEnhet
							* avsenderMottaker.navn
							* tittel på journalpostnivå
							* tittel på alle dokumentene
							
							NB: Dersom dokumentene skal være mulig å distribuere via Dokdist, eller skal kunne vises til brukeren på nav.no, må i tillegg avsenderMottaker.id og avsenderMottaker.idType settes.
							""",
					schema = @Schema(type = "boolean", allowableValues = {"true", "false"})
			)
			@RequestParam(required = false) String forsoekFerdigstill) {
		try {
			MDC.put(MDC_REQUEST_ID, "rjoark202");
			RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));

			log.info("{} har mottatt kall for opprettelse av ny journalpost", MDC.get(MDC_REQUEST_ID));
			try {
				opprettJournalpostRequestValidator.validateRequest(request, forsoekFerdigstill);
			} catch (InputValideringFeiletException e) {
				log.warn("rjoark202 feilet under validering. {}", e.getMessage(), e);
				throw e;
			}

			OpprettJournalpostResult opprettJournalpostResult = opprettJournalpostService.opprettJournalpost(request);

			List<DokumentInfoId> dokumenter = new ArrayList<>();
			opprettJournalpostResult.getJournalpost().getJournalpostDokumentInfoRelasjoner().forEach(
					journalpostDokumentInfoRelasjon -> dokumenter.add(DokumentInfoId.builder()
							.dokumentInfoId(journalpostDokumentInfoRelasjon.getDokumentInfo()
									.getDokumentInfoId()
									.toString())
							.build())
			);

			Long journalpostId = opprettJournalpostResult.getJournalpost().getJournalpostId();
			HttpStatus httpStatus = opprettJournalpostResult.isAlreadyOpprettet() ? CONFLICT : CREATED;

			Optional<Pair<String, String>> ferdigstillResponse = Optional.empty();
			if (TRUE.equalsIgnoreCase(forsoekFerdigstill)) {
				ferdigstillResponse = Optional.of(ferdigstillJournalpostService.forsoekFerdigstill(journalpostId, request));
			}

			String journalForendeEnhetId = opprettJournalpostResult.getJournalpost().getJournalForendeEnhetId();
			String httpResponse = ferdigstillResponse.map(Pair::getKey).orElse(null);

			if (TRUE.equalsIgnoreCase(forsoekFerdigstill) && MASKINELL_JOURNALFOERENDE_ENHET.equals(journalForendeEnhetId) && MIDLERTIDIG.equals(httpResponse)) {
				ferdigstillJournalpostService.setJournalfoerendeEnhetNull(journalpostId);
			}

			return ResponseEntity
					.status(httpStatus)
					.body(OpprettJournalpostResponse.builder()
							.journalpostId(valueOf(journalpostId))
							.journalstatus(ferdigstillResponse.map(Pair::getKey).orElse(opprettJournalpostResult.getJournalpost().getJournalstatus().name()))
							.melding(ferdigstillResponse.map(Pair::getValue).orElse(null))
							.journalpostferdigstilt(ferdigstillResponse.map(Pair::getKey)
									.filter(STATUS_ENDELIG::equalsIgnoreCase)
									.isPresent())
							.dokumenter(dokumenter)
							.build());
		} catch (InputValideringFeiletException | InvalidPdfException | UgyldigInputException e) {
			throw new ResponseStatusException(BAD_REQUEST, format("Kunne ikke opprette journalpost. %s", e.getMessage()));
		}
	}

	@Transactional
	@SwaggerFjernVedlegg
	@PatchMapping("/{journalpostId}/fjernVedlegg")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "fjernVedleggTilknyttetJournalpost"}, percentiles = {0.5, 0.95})
	public ResponseEntity<String> fjernVedleggTilknyttetJournalpost(@PathVariable String journalpostId,
																	@RequestBody FjernVedleggTilknyttetJournalpostRequest request) {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "fjernVedleggTilknyttetJournalpost");
		long journalpostIdParsed = validateIdAndParse(journalpostId, "tilknyttJournalpostId");
		MDC.put(MDC_JOURNALPOST_ID, String.valueOf(journalpostIdParsed));
		try {
			RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
			log.info("Fjerne vedlegg med dokumentinfoId={} som er knyttet til journalpost med journalpostId={}", request.getDokumentId(), journalpostIdParsed);
			fjernVedleggTilknyttJournalpost.fjernVedleggTilknyttetJournalpost(journalpostIdParsed, request);
			log.info("Vedlegg med dokumentinfoId={} som er knyttet til journalpost med journalpostId={} er fjernet", request.getDokumentId(), journalpostIdParsed);

			return ResponseEntity.ok()
					.contentType(APPLICATION_JSON)
					.body("\"Vedlegg som knyttet til journalposten fjernet\"");

		} catch (InputValideringFeiletException | KanIkkeSlettetVedleggKnyttetTilJournalpostException e) {
			String message = format("Kunne ikke fjerne vedlegg med dokumentinfoId=%s fra journalpost med journalpostId=%s. %s",
					request.getDokumentId(), journalpostIdParsed, e.getMessage());
			throw new ResponseStatusException(BAD_REQUEST, message);
		} catch (JournalpostIkkeFunnetException | DokumentIkkeFunnetException |
				 JournalpostDokumentInfoRelasjonIkkeFunnetException e) {
			String message = format("Kunne ikke fjerne vedlegg med dokumentinfoId=%s fra journalpost med journalpostId=%s. %s",
					request.getDokumentId(), journalpostIdParsed, e.getMessage());
			throw new ResponseStatusException(NOT_FOUND, message);
		}
	}

	@Transactional
	@PostMapping("/kopierJournalpost")
	@SwaggerKopierJournalpost
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "kopierJournalpost"}, percentiles = {0.5, 0.95}, histogram = true)
	public ResponseEntity<KopierJournalpostResponse> kopierJournalpost(
			@Parameter(
					name = "kildeJournalpostId",
					description = "Angir kildeJournalpostId som skal kopieres",
					required = true,
					example = "467011764"
			)
			@RequestParam String kildeJournalpostId,
			@RequestBody KopierJournalpostRequest request) {

		MDC.put(MDC_REQUEST_ID, "kopierJournalpost");
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));

		long kildeJournalpostIdParsed = validateIdAndParse(kildeJournalpostId, "kildeJournalpostId");
		validateEksternReferanseId(request.getEksternReferanseId());
		try {

			log.info("kopierJournalpost har mottatt kall for kopiere journalpost med journalpostId={}, eksternReferanseId={}", kildeJournalpostIdParsed, request.getEksternReferanseId());

			KopierJournalpostResult kopierJournalpostResult = kopierJournalpostService.kopierJournalpost(kildeJournalpostIdParsed, request.getEksternReferanseId());

			if (kopierJournalpostResult.duplikatEksternReferanseId()) {
				return ResponseEntity.status(CONFLICT)
						.body(KopierJournalpostResponse.builder()
								.kopierJournalpostId(valueOf(kopierJournalpostResult.kopierJournalpostId()))
								.build()
						);
			} else {
				return ResponseEntity.status(CREATED)
						.body(KopierJournalpostResponse.builder()
								.kopierJournalpostId(valueOf(kopierJournalpostResult.kopierJournalpostId()))
								.build()
						);
			}

		} catch (JournalpostIkkeFunnetException e) {
			String message = format("Kunne ikke finne journalpost med journalpostId=%s i joark", kildeJournalpostIdParsed);
			throw new ResponseStatusException(NOT_FOUND, message);
		} catch (KanIkkeKopiereException | IllegalArgumentException e) {
			throw new ResponseStatusException(BAD_REQUEST, e.getMessage());
		}
	}

	@Transactional
	@SwaggerLastOppVedlegg
	@PatchMapping("/{journalpostId}/lastOppVedlegg")
	public ResponseEntity<LastOppVedleggResponse> lastOppVedlegg(
			@Parameter(
					name = "journalpostId",
					description = "Angir JournalpostId for journalpost vedlegget skal legges til",
					required = true,
					example = "467011764"
			)
			@PathVariable String journalpostId,
			@RequestBody LastOppVedleggRequest request
	) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		MDC.put(MDC_REQUEST_ID, "lastOppVedlegg");
		long journalpostIdParsed = validateIdAndParse(journalpostId, "journalpostId");
		MDC.put(MDC_JOURNALPOST_ID, String.valueOf(journalpostIdParsed));

		log.info("lastOppVedlegg har mottatt kall om å legge til vedlegg på journalpost med journalpostId={}", journalpostIdParsed);

		try {
			LastOppVedleggValidator.validateRequest(request);

			LastOppVedleggResponse response = lastOppVedleggService.lastOppVedlegg(journalpostIdParsed, request);

			log.info("lastOppVedlegg har lagt til vedlegg med dokumentInfoId={} på journalpost med journalpostId={}",
					response.dokumentInfoId(), journalpostIdParsed);

			return ResponseEntity
					.status(CREATED)
					.body(response);

		} catch (InputValideringFeiletException e) {
			throw new ResponseStatusException(BAD_REQUEST,
					"Kunne ikke legge til vedlegg på journalpost med journalpostId=%s. Validering av input feilet: %s"
							.formatted(journalpostIdParsed, e.getMessage()));
		} catch (KanIkkeLeggeTilVedleggException e) {
			throw new ResponseStatusException(CONFLICT,
					"Kunne ikke legge til vedlegg på journalpost med journalpostId=%s. %s"
							.formatted(journalpostIdParsed, e.getMessage()));
		}
	}

}
