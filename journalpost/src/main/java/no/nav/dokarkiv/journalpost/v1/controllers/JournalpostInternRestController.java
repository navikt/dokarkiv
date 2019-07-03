package no.nav.dokarkiv.journalpost.v1.controllers;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CALL_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.util.DecodeUtils.decodeBasicAuth;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateId;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.FeiletDokument;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggResponse;
import no.nav.dokarkiv.journalpost.v1.services.TilknyttVedleggService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerTilknyttVedlegg;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggRequestValidator;
import org.slf4j.MDC;
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

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */

@Slf4j
@RestController
@RequestMapping("/rest/intern/journalpostapi/v1/journalpost")
public class JournalpostInternRestController {

	private final TilknyttVedleggService tilknyttVedleggService;
	private final TilknyttVedleggRequestValidator tilknyttVedleggRequestValidator;
	private static final String SRVDOKARKIVPROXY = "srvdokarkivproxy";

	@Inject
	public JournalpostInternRestController(final TilknyttVedleggService tilknyttVedleggService) {
		this.tilknyttVedleggService = tilknyttVedleggService;
		this.tilknyttVedleggRequestValidator = new TilknyttVedleggRequestValidator();

	}

	@Transactional
	@SwaggerTilknyttVedlegg
	@ResponseBody
	@PutMapping(value = "/{journalpostId}/tilknyttVedlegg")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "tilknyttVedlegg"}, percentiles = {0.5, 0.95})
	public ResponseEntity<TilknyttVedleggResponse> tilknyttVedlegg(
			@PathVariable String journalpostId,
			@RequestHeader(value = "callId", required = false) String callId,
			@RequestHeader(value = "Authorization") String auth,
			@RequestBody TilknyttVedleggRequest request) throws IOException {


		try {
			if (SRVDOKARKIVPROXY.equals(decodeBasicAuth(auth)[0])) {
				addValueToMDC(callId, MDC_CALL_ID);
				validateId(journalpostId, "journalpostId");

				RequestContextUtil.createAndSetUsername("tilknyttVedlegg", "dokarkiv");
				MDC.put(MDC_REQUEST_ID, "tilknyttVedlegg");

				log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall om å legge til vedlegg på journalpostId={}", journalpostId);

				tilknyttVedleggRequestValidator.validateRequest(request);

				List<FeiletDokument> feiletDokumentList = tilknyttVedleggService.tilknyttVedlegg(Long.parseLong(journalpostId), request);

				if (feiletDokumentList.isEmpty()) {
					return ResponseEntity
							.ok()
							.body(TilknyttVedleggResponse.builder().build());
				} else {
					return ResponseEntity
							.status(HttpStatus.MULTI_STATUS)
							.body(TilknyttVedleggResponse.builder().feiletDokument(feiletDokumentList).build());
				}
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(TilknyttVedleggResponse.builder().build());
			}

		} catch (DokarkivFunctionalException e) {
			log.warn("tilknyttVedlegg - feilet funksjonelt ved tilknytning av vedlegg for journalpostId={}. Feilmelding={}", journalpostId, e
					.getMessage());
			throw e;
		} catch (DokarkivTechnicalException e) {
			log.warn("tilknyttVedlegg - feilet teknisk ved tilknytning av vedlegg for journalpostId={}. Feilmelding={}", journalpostId, e
					.getMessage());
			throw e;
		}
	}

	private void addValueToMDC(String value, String key) {
		if (value != null && !value.isEmpty()) {
			MDC.put(key, value);
		}
	}

}
