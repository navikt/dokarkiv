package no.nav.dokarkiv.journalfoerinngaaende.v1;


import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.READ_ACTION;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.tjenester.journalfoerinngaaende.GetJournalpostResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.PostLogiskVedleggRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PostLogiskVedleggResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.PutDokumentRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutDokumentResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.PutLogiskVedleggRequest;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.exceptions.DokarkivRestFunctionalException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.security.abac.AuthorizationException;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalfoerinngaaende.v1.util.Utils;
import no.nav.dokarkiv.journalfoerinngaaende.v1.service.GetInngaaendeJournalpostService;
import no.nav.dokarkiv.journalfoerinngaaende.v1.service.LogiskVedleggService;
import no.nav.dokarkiv.journalfoerinngaaende.v1.service.PersistInngaaendeJournalpostService;
import no.nav.dokarkiv.journalfoerinngaaende.v1.service.UpdateInngaaendeJournalpostDokumentService;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;


/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@RestController
@RequestMapping("/rest/journalfoer-inngaaende/v1/journalposter")
public class JournalfoerInngaaendeRestController {

	private GetInngaaendeJournalpostService getInngaaendeJournalpostService;
	private PersistInngaaendeJournalpostService persistInngaaendeJournalpostService;
	private LogiskVedleggService logiskVedleggService;
	private AbacSecurityService abacSecurityService;
	private UpdateInngaaendeJournalpostDokumentService updateInngaaendeJournalpostDokumentService;

	@Inject
	public JournalfoerInngaaendeRestController(GetInngaaendeJournalpostService getInngaaendeJournalpostService,
											   PersistInngaaendeJournalpostService persistInngaaendeJournalpostService,
											   LogiskVedleggService logiskVedleggService,
											   AbacSecurityService abacSecurityService,
											   UpdateInngaaendeJournalpostDokumentService updateInngaaendeJournalpostDokumentService) {
		this.getInngaaendeJournalpostService = getInngaaendeJournalpostService;
		this.abacSecurityService = abacSecurityService;
		this.updateInngaaendeJournalpostDokumentService = updateInngaaendeJournalpostDokumentService;
		this.persistInngaaendeJournalpostService = persistInngaaendeJournalpostService;
		this.logiskVedleggService = logiskVedleggService;
	}

	@GetMapping("/{journalpostId}")
	@Transactional(readOnly = true)
	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)})
	public ResponseEntity getInngaaendeJournalpostByJournalpostId(@PathVariable String journalpostId) {
		try {
			validateId(journalpostId, "journalpostId");
			assertAccessToJournalpost(journalpostId);
			GetJournalpostResponse responseTo = getInngaaendeJournalpostService.getInngaaendeJournalpostByJournalpostId(journalpostId);
			log.info("Hentet journalpost med journalpostId={}, dokumentinfoId(er)={} og dokumenttypeId(er)={} fra Joark.",
					journalpostId, Utils.getDokumentIds(responseTo), Utils.getDokumenttypeIds(responseTo));
			return new ResponseEntity<>(responseTo, HttpStatus.OK);
		} catch (DokarkivRestFunctionalException e) {
			log.warn("Feilmelding={}, journalpostId={}. HttpStatus={}", e.getMessage(), journalpostId, e.getHttpStatus());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_PLAIN);
			return new ResponseEntity<>(e.getMessage() + ". journalpostId=" + journalpostId, headers, e.getHttpStatus());
		}
	}

	@PutMapping(value = "/{journalpostId}")
	@Transactional
	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)})
	public ResponseEntity persistInngaaendeJournalpost(@PathVariable String journalpostId, @RequestBody PutJournalpostRequest request) {
		try {
			validateId(journalpostId, "journalpostId");
			assertAccessToJournalpost(journalpostId);
			RequestContextUtil.createAndSetUsername("bruker", "consumerId"); //TODO: Disse feltene må settes!
			PutJournalpostResponse inngaaendeResponseTo = persistInngaaendeJournalpostService.persist(journalpostId, request);
			log.info("Oppdatert journalpost med journalpostId={} i Joark.", journalpostId);
			return new ResponseEntity<>(inngaaendeResponseTo, HttpStatus.OK);
		} catch (DokarkivRestFunctionalException e) {
			log.warn("Feilmelding={}, journalpostId={}. HttpStatus={}", e.getMessage(), journalpostId, e.getHttpStatus());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_PLAIN);
			return new ResponseEntity<>(e.getMessage() + ". journalpostId=" + journalpostId, headers, e.getHttpStatus());
		}
	}

	@DeleteMapping(value = "{journalpostId}/dokumenter/{dokumentId}/logiskeVedlegg/{logiskVedleggId}")
	@Transactional
	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)})
	public ResponseEntity deleteLogiskVedlegg(@PathVariable String journalpostId, @PathVariable String dokumentId, @PathVariable String logiskVedleggId) {
		try {
			validateId(journalpostId, "journalpostId");
			validateId(dokumentId, "dokumentId");
			validateId(logiskVedleggId, "logiskVedleggId");
			RequestContextUtil.createAndSetUsername("bruker", "consumerId"); //TODO: Disse feltene må settes!
			assertAccessToJournalpost(journalpostId);
			logiskVedleggService.deleteLogiskVedlegg(journalpostId, dokumentId, logiskVedleggId);
			log.info("Slettet logisk vedlegg fra journalpost, journalpostId={}, dokumentinfoId={}, logiskVedleggId={}.", journalpostId, dokumentId, logiskVedleggId);
			return new ResponseEntity<>(String.format("Sleting av logiskVedlegg med logiskVedleggId=%s var vellykket. journalpostId=%s, dokumentId=%s",
					logiskVedleggId, journalpostId, dokumentId), HttpStatus.OK);
		} catch (DokarkivRestFunctionalException e) {
			log.warn("Feilmelding={}, journalpostId={}. HttpStatus={}", e.getMessage(), journalpostId, e.getHttpStatus());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_PLAIN);
			return new ResponseEntity<>(e.getMessage() + ". journalpostId=" + journalpostId, headers, e.getHttpStatus());
		}
	}


	@PutMapping(value = "{journalpostId}/dokumenter/{dokumentId}/logiskeVedlegg/{logiskVedleggId}")
	@Transactional
	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)})
	public ResponseEntity updateLogiskVedlegg(@PathVariable String journalpostId, @PathVariable String dokumentId, @PathVariable String logiskVedleggId, @RequestBody PutLogiskVedleggRequest request) {
		try {
			validateId(journalpostId, "journalpostId");
			validateId(dokumentId, "dokumentId");
			validateId(logiskVedleggId, "logiskVedleggId");
			RequestContextUtil.createAndSetUsername("bruker", "consumerId"); //TODO: Disse feltene må settes!
			assertAccessToJournalpost(journalpostId);
			logiskVedleggService.updateLogiskVedlegg(journalpostId, dokumentId, logiskVedleggId, request);
			log.info("tjoark070 oppdaterte logisk vedlegg på journalpost, journalpostId={}, dokumentinfoId={}, logiskVedleggId={}.", journalpostId, dokumentId, logiskVedleggId);
			return new ResponseEntity<>(String.format("Oppdatering av logiskVedlegg med logiskVedleggId=%s var vellykket. journalpostId=%s, dokumentId=%s",
					logiskVedleggId, journalpostId, dokumentId), HttpStatus.OK);
		} catch (DokarkivRestFunctionalException e) {
			log.warn("Feilmelding={}, journalpostId={}. HttpStatus={}", e.getMessage(), journalpostId, e.getHttpStatus());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_PLAIN);
			return new ResponseEntity<>(e.getMessage() + ". journalpostId=" + journalpostId, headers, e.getHttpStatus());
		}
	}


	@PostMapping(value = "{journalpostId}/dokumenter/{dokumentId}/logiskeVedlegg}")
	@Transactional
	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)})
	public ResponseEntity persistLogiskVedlegg(@PathVariable String journalpostId, @PathVariable String dokumentId, @RequestBody PostLogiskVedleggRequest request) {
		try {
			validateId(journalpostId, "journalpostId");
			validateId(dokumentId, "dokumentId");
			RequestContextUtil.createAndSetUsername("bruker", "consumerId"); //TODO: Disse feltene må settes!
			assertAccessToJournalpost(journalpostId);
			Long logiskVedleggId = logiskVedleggService.persistLogiskVedlegg(journalpostId, dokumentId, request);
			log.info(String.format("tjoark070 persisterte logiskVedlegg med logiskVedleggId=%s. journalpostId=%s, dokumentId=%s",
					logiskVedleggId.toString(), journalpostId, dokumentId));
			return new ResponseEntity<>(new PostLogiskVedleggResponse().withLogiskVedleggId(logiskVedleggId.toString()), HttpStatus.OK);
		} catch (DokarkivRestFunctionalException e) {
			log.warn("Feilmelding={}, journalpostId={}. HttpStatus={}", e.getMessage(), journalpostId, e.getHttpStatus());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_PLAIN);
			return new ResponseEntity<>(e.getMessage() + ". journalpostId=" + journalpostId, headers, e.getHttpStatus());
		}
	}

	@PutMapping(value = "/{journalpostId}/dokumenter/{dokumentid}")
	@Transactional
	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)})
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "tjoark070"}, percentiles = {0.5, 0.95})
	public @ResponseBody
	PutDokumentResponse updateDokument(@PathVariable String journalpostId, @PathVariable String dokumentid, @RequestBody PutDokumentRequest request) {
		log.info("tjoark070 har mottat kall om å oppdatere dokument med journalpostId={} og dokumentId={}", journalpostId, dokumentid);
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		PutDokumentResponse inngaaendeResponseTo = updateInngaaendeJournalpostDokumentService.update(journalpostId, dokumentid, request);
		log.info("tjoark070 har oppdatert dokument med journalpostId={} og dokumentId={} i Joark.", journalpostId, dokumentid);
		return inngaaendeResponseTo;

	}

	private void assertAccessToJournalpost(String journalpostId) throws DokarkivRestFunctionalException {
		try {
			abacSecurityService.assertAccessToJournalpost(journalpostId);
		} catch (AuthorizationException e) {
			throw new DokarkivRestFunctionalException(e.getMessage(), HttpStatus.FORBIDDEN);
		} catch (JournalpostIkkeFunnetException e) {
			throw new DokarkivRestFunctionalException("Kunne ikke finne journalpost i Joark", HttpStatus.NOT_FOUND);
		}
	}

	private void validateId(String journalpostId, String feltnavn) {
		Utils.hasText(journalpostId, feltnavn);
		Utils.convertStringToLong(journalpostId, feltnavn);
	}

}
