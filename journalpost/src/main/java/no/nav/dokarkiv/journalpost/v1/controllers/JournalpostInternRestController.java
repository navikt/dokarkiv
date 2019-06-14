package no.nav.dokarkiv.journalpost.v1.controllers;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateId;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.FeiletDokument;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggResponse;
import no.nav.dokarkiv.journalpost.v1.services.TilknyttVedleggService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerOppdaterJournalpost;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggRequestValidator;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggValidator;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import javax.inject.Inject;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */

@Api
@Slf4j
@RestController
@RequestMapping("/rest/intern/journalpostapi/v1/journalpost")
public class JournalpostInternRestController {

	private final TilknyttVedleggService tilknyttVedleggService;
	private final TilknyttVedleggRequestValidator tilknyttVedleggRequestValidator;

	@Inject
	public JournalpostInternRestController(final TilknyttVedleggService tilknyttVedleggService) {
		this.tilknyttVedleggService = tilknyttVedleggService;

		this.tilknyttVedleggRequestValidator = new TilknyttVedleggRequestValidator();

	}

	@Transactional
	@ResponseBody
	@PutMapping(value = "/{journalpostId}/tilknyttVedlegg")
	//@RestMetrics(value = "dok_request", extraTags = {"process_code", "tilknyttVedlegg"}, percentiles = {0.5, 0.95})
	public ResponseEntity<TilknyttVedleggResponse> tilknyttVedlegg(
			@PathVariable String journalpostId,
			@RequestBody TilknyttVedleggRequest request) {
		RequestContextUtil.createAndSetUsername("test", "test");
		MDC.put(MDC_REQUEST_ID, "tilknyttVedlegg");
		log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall om å legge til vedlegg på journalpostId={}", journalpostId);
		//validateId(journalpostId, "journalpostId");
		//abacSecurityService.assertAccessToJournalpost(journalpostId);

		tilknyttVedleggRequestValidator.validateRequest(request);

		List<FeiletDokument> feiletDokumentList = tilknyttVedleggService.tilknyttVedlegg(Long.parseLong(journalpostId), request);

		if (feiletDokumentList == null) {
			log.info("tilknyttVedlegg har lagt til vedlegg på journalpost med journalpostId={} i Joark.", journalpostId);
			return ResponseEntity
					.ok()
					.body(TilknyttVedleggResponse.builder().build());
		} else {
			return ResponseEntity
					.status(HttpStatus.MULTI_STATUS)
					.body(TilknyttVedleggResponse.builder().feiletDokument(feiletDokumentList).build());
		}

	}
}
