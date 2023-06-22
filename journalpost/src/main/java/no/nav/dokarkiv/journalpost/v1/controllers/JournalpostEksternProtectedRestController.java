package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.exceptions.KanIkkeFerdigstilleException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.FeiledeDokumenter;
import no.nav.dokarkiv.journalpost.v1.api.KnyttTilAnnenSakRequest;
import no.nav.dokarkiv.journalpost.v1.api.KnyttTilAnnenSakResponse;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggResponse;
import no.nav.dokarkiv.journalpost.v1.services.KnyttTilAnnenSakService;
import no.nav.dokarkiv.journalpost.v1.services.TilknyttVedleggService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerRestKnyttTilAnnenSak;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerRestTilknyttVedlegg;
import no.nav.dokarkiv.journalpost.v1.validators.KnyttTilAnnenSakValidator;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateId;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@Protected
@RestController
@RequestMapping("/rest/journalpostapi/v1/journalpost")
public class JournalpostEksternProtectedRestController {

	private final KnyttTilAnnenSakValidator knyttTilAnnenSakValidator;
	private final KnyttTilAnnenSakService knyttTilAnnenSakService;

	private final TilknyttVedleggService tilknyttVedleggService;

	public JournalpostEksternProtectedRestController(KnyttTilAnnenSakValidator knyttTilAnnenSakValidator,
													 KnyttTilAnnenSakService knyttTilAnnenSakService,
													 TilknyttVedleggService tilknyttVedleggService) {
		this.knyttTilAnnenSakValidator = knyttTilAnnenSakValidator;
		this.knyttTilAnnenSakService = knyttTilAnnenSakService;
		this.tilknyttVedleggService = tilknyttVedleggService;
	}


	@Transactional
	@SwaggerRestKnyttTilAnnenSak
	@Operation(summary = "Knytt dokumenter til ny sak.")
	@PutMapping("/{kildeJournalpostId}/knyttTilAnnenSak")
	@Tag(name = "journalpostapi - knyttTilAnnenSak", description = "Tjeneste for å endre sakstilknytning på en journalpost")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "knyttTilAnnenSak"}, percentiles = {0.5, 0.95})
	public ResponseEntity<KnyttTilAnnenSakResponse> knyttTilAnnenSak(@Parameter(hidden = true) @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorizationHeader,
																	 @Parameter(description = "Nav-Consumer-Token - Systembrukerens OIDC-token. NB: Oppgis kun dersom den NAV-ansattes token er lagt ved under Authorization") @RequestHeader(value = "Nav-Consumer-Token", required = false) String navConsumerToken,
																	 @Parameter(description = "Nav-CallId - teknisk sporingsid") @RequestHeader(value = "Nav-CallId", required = false) String navCallId,
																	 @Parameter(description = "ID til journalposten som det er ønskelig å kopiere", required = true) @PathVariable String kildeJournalpostId,
																	 @RequestBody KnyttTilAnnenSakRequest knyttTilAnnenSakRequest) {

		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		try {
			log.info("knyttTilAnnenSak har fått har fått kall for å knytte dokumenter til annen sak");
			knyttTilAnnenSakValidator.validate(knyttTilAnnenSakRequest, kildeJournalpostId);
			KnyttTilAnnenSakResponse knyttTilAnnenSakResponse = knyttTilAnnenSakService.knyttTilAnnenSak(knyttTilAnnenSakRequest, Long.parseLong(kildeJournalpostId));

			log.info("knyttTilAnnenSak har knyttet til dokumenter til ny journalpost med journalpostId={}", knyttTilAnnenSakResponse.getNyJournalpostId());

			return ResponseEntity.ok().body(knyttTilAnnenSakResponse);
		} catch (KanIkkeFerdigstilleException e) {
			throw new ResponseStatusException(BAD_REQUEST,
					format("knyttTilAnnenSak kunne ikke knytte dokumenter til annen sak for journalpostId=%s. %s", kildeJournalpostId, e.getMessage()),
					e);
		} catch (DokarkivFunctionalException e) {
			log.warn("knyttTilAnnenSak - feilet funksjonelt ved knytning dokumenter til annen sak for journalpostId={} med Feilmelding={}", kildeJournalpostId,
					e.getMessage());
			throw e;
		} catch (DokarkivTechnicalException e) {
			log.warn("knyttTilAnnenSak - feilet teknisk ved knytning dokumenter til annen sak for journalpostId={} med Feilmelding={}", kildeJournalpostId, e
					.getMessage());
			throw e;
		}
	}

	@Transactional
	@SwaggerRestTilknyttVedlegg
	@ResponseBody
	@Operation(summary = "Knytt vedlegg til journalpost")
	@Tag(name = "journalpostapi - tilknyttVedlegg", description = "Tjeneste for å knytte vedlegg til en journalpost")
	@PutMapping(value = "/{journalpostId}/tilknyttVedlegg")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "tilknyttVedlegg"}, percentiles = {0.5, 0.95})
	public ResponseEntity<TilknyttVedleggResponse> tilknyttVedlegg(
			@Parameter(hidden = true) @RequestHeader(value = HttpHeaders.AUTHORIZATION) String auth,
			@PathVariable String journalpostId,
			@RequestBody TilknyttVedleggRequest request) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		MDC.put(MDC_REQUEST_ID, "tilknyttVedlegg");
		try {
			validateId(journalpostId, "journalpostId");
			long journalpostIdLong = Long.parseLong(journalpostId);

			log.info("tilknyttVedlegg har mottatt kall om å legge til vedlegg på journalpostId={}", journalpostId);

			List<FeiledeDokumenter> feiledeDokumenterList = tilknyttVedleggService.tilknyttVedlegg(journalpostIdLong, request);

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
}