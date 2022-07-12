package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.FerdigstillJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.FjernVedleggTilknyttetJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.KnyttTilAnnenSakRequest;
import no.nav.dokarkiv.journalpost.v1.api.KnyttTilAnnenSakResponse;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterDistribusjonsinfoRequest;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.DokumentInfoId;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostResult;
import no.nav.dokarkiv.journalpost.v1.services.FerdigstillJournalpostService;
import no.nav.dokarkiv.journalpost.v1.services.FjernVedleggTilknyttetJournalpost;
import no.nav.dokarkiv.journalpost.v1.services.KnyttTilAnnenSakService;
import no.nav.dokarkiv.journalpost.v1.services.OppdaterDistribusjonsinfoService;
import no.nav.dokarkiv.journalpost.v1.services.OppdaterJournalpostService;
import no.nav.dokarkiv.journalpost.v1.services.OpprettJournalpostService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFerdigstillJournalpost;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFjernVedlegg;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerOppdaterDistribusjonsinfo;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerOppdaterJournalpost;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerOpprettJournalpost;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerRestKnyttTilAnnenSak;
import no.nav.dokarkiv.journalpost.v1.validators.FerdigstillJournalpostValidator;
import no.nav.dokarkiv.journalpost.v1.validators.KnyttTilAnnenSakValidator;
import no.nav.dokarkiv.journalpost.v1.validators.OppdaterDistribusjonsinfoValidator;
import no.nav.dokarkiv.journalpost.v1.validators.OpprettJournalpostRequestValidator;
import no.nav.security.token.support.core.api.Protected;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateId;
import static no.nav.dokarkiv.journalpost.v1.validators.OpprettJournalpostRequestValidator.MASKINELL_JOURNALFOERENDE_ENHET;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;

@Tag(name = "journalpostapi", description = "Tjenester for å arkivere og journalføre i fagarkiv")
@Slf4j
@Protected
@RestController
@RequestMapping("/rest/journalpostapi/v1/journalpost")
public class ArkiverOgJournalfoerRestController {

    private static final String TRUE = "true";
    private static final String MIDLERTIDIG = "MIDLERTIDIG";
    private static final String STATUS_ENDELIG = "ENDELIG";
    private static final String BIDRAG_NAV_CONSUMER_ID = "dialogstyring-bidrag";
    private final FerdigstillJournalpostService ferdigstillJournalpostService;
    private final OppdaterJournalpostService oppdaterJournalpostService;
    private final OppdaterDistribusjonsinfoService oppdaterDistribusjonsinfoService;
    private final OpprettJournalpostService opprettJournalpostService;
    private final OpprettJournalpostRequestValidator opprettJournalpostRequestValidator;
    private final FerdigstillJournalpostValidator ferdigstillJournalpostValidator;
    private final OppdaterDistribusjonsinfoValidator oppdaterDistribusjonsinfoValidator;
    private final FjernVedleggTilknyttetJournalpost fjernVedleggTilknyttJournalpost;
    private final KnyttTilAnnenSakValidator knyttTilAnnenSakValidator;
    private final KnyttTilAnnenSakService knyttTilAnnenSakService;

    @Inject
    public ArkiverOgJournalfoerRestController(final FerdigstillJournalpostService ferdigstillJournalpostService,
                                              final OppdaterJournalpostService oppdaterJournalpostService,
                                              final OpprettJournalpostService opprettJournalpostService,
                                              final OppdaterDistribusjonsinfoService oppdaterDistribusjonsinfoService,
                                              final FjernVedleggTilknyttetJournalpost fjernVedleggTilknyttJournalpost,
                                              final KnyttTilAnnenSakValidator knyttTilAnnenSakValidator,
                                              final KnyttTilAnnenSakService knyttTilAnnenSakService) {
        this.ferdigstillJournalpostService = ferdigstillJournalpostService;
        this.oppdaterJournalpostService = oppdaterJournalpostService;
        this.opprettJournalpostService = opprettJournalpostService;
        this.fjernVedleggTilknyttJournalpost = fjernVedleggTilknyttJournalpost;
        this.oppdaterDistribusjonsinfoService = oppdaterDistribusjonsinfoService;
        this.knyttTilAnnenSakValidator = knyttTilAnnenSakValidator;
        this.knyttTilAnnenSakService = knyttTilAnnenSakService;
        this.opprettJournalpostRequestValidator = new OpprettJournalpostRequestValidator();
        this.ferdigstillJournalpostValidator = new FerdigstillJournalpostValidator();
        this.oppdaterDistribusjonsinfoValidator = new OppdaterDistribusjonsinfoValidator();
    }

	@Transactional
	@SwaggerFerdigstillJournalpost
	@PatchMapping("/{journalpostId}/ferdigstill")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark201"}, percentiles = {0.5, 0.95})
	public ResponseEntity<String> ferdigstillJournalpost(
			@PathVariable @Parameter(description = "IDen til journalposten som skal ferdigstilles", required = true, example = "77778888") String journalpostId,
			@RequestBody FerdigstillJournalpostRequest request
	) {
		try {
			MDC.put(MDC_REQUEST_ID, "rjoark201");
			log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall for ferdigstilling av journalpost med journalpostId={}", journalpostId);
			ferdigstillJournalpostValidator.validateRequest(journalpostId, request);
			RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

			ferdigstillJournalpostService.ferdigstill(Long.parseLong(journalpostId), request);
			log.info(MDC.get(MDC_REQUEST_ID) + " har ferdigstilt journalpost med journalpostId={}", journalpostId);

			return ResponseEntity.ok().body("Journalpost ferdigstilt");
		} finally {
			MDC.clear();
		}
	}

	@Transactional
	@SwaggerOppdaterDistribusjonsinfo
	@PatchMapping("/{journalpostId}/oppdaterDistribusjonsinfo")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark201"}, percentiles = {0.5, 0.95})
	public ResponseEntity<String> oppdaterDistribusjonsinfo(
			@PathVariable @Parameter(description = "IDen til journalposten som skal oppdateres", required = true, example = "77778888") String journalpostId,
			@RequestBody OppdaterDistribusjonsinfoRequest request) {
		try {
			MDC.put(MDC_REQUEST_ID, "oppdaterDistribusjonsinfo");
			log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall for oppdatering av distribusjonsinfo for journalpostId={}", journalpostId);
			validateId(journalpostId, "journalpostId");
			oppdaterDistribusjonsinfoValidator.validateRequest(journalpostId, request);
			RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

			oppdaterDistribusjonsinfoService.oppdaterDistribusjonsinfo(Long.parseLong(journalpostId), request);

			log.info(MDC.get(MDC_REQUEST_ID) + " har oppdatert distribusjonsinfo på journalpost med journalpostId={}", journalpostId);

			return ResponseEntity.ok().body("Journalpost oppdatert");
		} finally {
			MDC.clear();
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
		try {
			RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
			MDC.put(MDC_REQUEST_ID, "oppdaterjournalpost");
			log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall om å oppdatere journalpost med journalpostId={}", journalpostId);
			validateId(journalpostId, "journalpostId");

			oppdaterJournalpostService.oppdaterJournalpost(Long.parseLong(journalpostId), request);

			log.info("oppdaterjournalpost har oppdatert journalpost med journalpostId={} i Joark.", journalpostId);
			return OppdaterJournalpostResponse.builder().journalpostId(journalpostId).build();
		} finally {
			MDC.clear();
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
							Angir hvorvidt tjenesten skal forsøke å ferdigstille eller ikke. Dette vil å sette journalposten i en status som indikerer at journalføring er komplett,
							 og låser journalposten for senere endringer. Journalposten blir uansett opprettet, men kun ferdigstilt dersom den oppfyller krav til struktur og metadata som beskrevet under ferdigstillJournalpost.
							 Dersom det feiler å ferdigstille journalposten og den har status "midlertidig" og journalførendeEnhet=="9999" skal journalførendeEnhet settes til null. Sjekk "journalpostferdigstilt" på responsen for å være sikker på at journalposten faktisk ble ferdigstilt.""",
					schema = @Schema(type = "boolean", allowableValues = {"true", "false"})
			)
			@RequestParam(required = false) String forsoekFerdigstill) {
		try {
			MDC.put(MDC_REQUEST_ID, "rjoark202");
			RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));

			log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall for opprettelse av ny journalpost");
			try {
				opprettJournalpostRequestValidator.validateRequest(request, forsoekFerdigstill);
			} catch (InputValideringFeiletException e) {
				log.warn("rjoark202 feilet under validering. " + e.getMessage(), e);
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
				ferdigstillJournalpostService.setJournalfoerendeEnhetNull(journalpostId, null);
			}


			return ResponseEntity
					.status(httpStatus)
					.body(OpprettJournalpostResponse.builder()
							.journalpostId(String.valueOf(journalpostId))
							.journalstatus(ferdigstillResponse.map(Pair::getKey).orElse(opprettJournalpostResult.getJournalpost().getJournalstatus().name()))
							.melding(ferdigstillResponse.map(Pair::getValue).orElse(null))
							.journalpostferdigstilt(ferdigstillResponse.map(Pair::getKey)
									.filter(STATUS_ENDELIG::equalsIgnoreCase)
									.isPresent())
							.dokumenter(dokumenter)
							.build());
		} finally {
			MDC.clear();
		}
	}

	@Transactional
	@SwaggerFjernVedlegg
	@PatchMapping("/{journalpostId}/fjernVedlegg")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "fjernVedleggTilknyttetJournalpost"}, percentiles = {0.5, 0.95})
	public ResponseEntity<String> fjernVedleggTilknyttetJournalpost(@PathVariable String journalpostId,
																	@RequestBody FjernVedleggTilknyttetJournalpostRequest request) {
		try {
			MDC.put(MDCConstants.MDC_REQUEST_ID, "fjernVedleggTilknyttetJournalpost");
			validateId(journalpostId, "tilknyttJournalpostId");
			RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
			log.info("Fjerne vedlegg med dokumentinfoId={} som er knyttet til journalpost med journalpostId={}", request.getDokumentId(), journalpostId);
			fjernVedleggTilknyttJournalpost.fjernVedleggTilknyttetJournalpost(journalpostId, request);
			log.info("Vedlegg med dokumentinfoId={} som er knyttet til journalpost med journalpostId={} er fjernet", request.getDokumentId(), journalpostId);
			return ResponseEntity.ok("Vedlegg som knyttet til journalposten fjernet");
		} finally {
			MDC.clear();
		}
	}

    @Transactional
    @SwaggerRestKnyttTilAnnenSak
    @Operation(summary = "Knytt dokumenter til ny sak.")
    @PutMapping("/{kildeJournalpostId}/knyttTilAnnenSak")
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "knyttTilAnnenSak"}, percentiles = {0.5, 0.95})
    public ResponseEntity<KnyttTilAnnenSakResponse> knyttTilAnnenSak(@Parameter(hidden = true) @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                                     @Parameter(description = "Nav-Consumer-Token - Systembrukerens OIDC-token. NB: Oppgis kun dersom den NAV-ansattes token er lagt ved under Authorization") @RequestHeader(value = "Nav-Consumer-Token", required = false) String navConsumerToken,
                                                                     @Parameter(description = "Nav-Consumer-Id - brukes for sporingsinfo i joark", required = true) @RequestHeader(value = "Nav-Consumer-Id") String navConsumerId,
                                                                     @Parameter(description = "Nav-CallId - teknisk sporingsid") @RequestHeader(value = "Nav-CallId", required = false) String navCallId,
                                                                     @Parameter(description = "ID til journalposten som det er ønskelig å kopiere", required = true) @PathVariable String kildeJournalpostId,
                                                                     @RequestBody KnyttTilAnnenSakRequest knyttTilAnnenSakRequest) {

        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
        try {
            log.warn("knyttTilAnnenSak har fått har fått kall for å knytte dokumenter til annen sak");
            knyttTilAnnenSakValidator.validateKnyttTilAnnenSakRequest(knyttTilAnnenSakRequest, kildeJournalpostId, navConsumerId);
            KnyttTilAnnenSakResponse knyttTilAnnenSakResponse = knyttTilAnnenSakService.knyttTilAnnenSak(knyttTilAnnenSakRequest, kildeJournalpostId, authorizationHeader, navConsumerToken);

            log.warn("knyttTilAnnenSak har knyttet til dokumenter til ny journalpost med journalpostId={}", knyttTilAnnenSakResponse.getNyJournalpostId());

            return ResponseEntity.ok().body(knyttTilAnnenSakResponse);

        } catch (DokarkivFunctionalException e) {
            log.warn("knyttTilAnnenSak - feilet funksjonelt ved knytning dokumenter til annen sak for journalpostId={}. Feilmelding={}", kildeJournalpostId, e
                    .getMessage());
            throw e;
        } catch (DokarkivTechnicalException e) {
            log.warn("knyttTilAnnenSak - feilet teknisk ved knytning dokumenter til annen sak for journalpostId={}. Feilmelding={}", kildeJournalpostId, e
                    .getMessage());
            throw e;
        }
    }

}
