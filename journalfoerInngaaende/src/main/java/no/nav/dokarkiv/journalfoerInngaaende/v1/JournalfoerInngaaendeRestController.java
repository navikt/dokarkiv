package no.nav.dokarkiv.journalfoerInngaaende.v1;


import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.READ_ACTION;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.Utils.convertStringToLong;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.Utils.getDokumentIds;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.Utils.getDokumenttypeIds;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.Utils.hasText;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.tjenester.journalfoerinngaaende.GetJournalpostResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostResponse;
import no.nav.dokarkiv.core.exceptions.DokarkivRestFunctionalException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.security.abac.AuthorizationException;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalfoerInngaaende.v1.service.DeleteLogiskVedleggService;
import no.nav.dokarkiv.journalfoerInngaaende.v1.service.GetInngaaendeJournalpostService;
import no.nav.dokarkiv.journalfoerInngaaende.v1.service.PersistInngaaendeJournalpostService;
import no.nav.freg.abac.core.annotation.Abac;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
	private DeleteLogiskVedleggService deleteLogiskVedleggService;
	private AbacSecurityService abacSecurityService;

	@Inject
	public JournalfoerInngaaendeRestController(GetInngaaendeJournalpostService getInngaaendeJournalpostService,
											   PersistInngaaendeJournalpostService persistInngaaendeJournalpostService,
											   DeleteLogiskVedleggService deleteLogiskVedleggService,
											   AbacSecurityService abacSecurityService) {
		this.getInngaaendeJournalpostService = getInngaaendeJournalpostService;
		this.abacSecurityService = abacSecurityService;
		this.persistInngaaendeJournalpostService = persistInngaaendeJournalpostService;
		this.deleteLogiskVedleggService = deleteLogiskVedleggService;
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
					journalpostId, getDokumentIds(responseTo), getDokumenttypeIds(responseTo));
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
			deleteLogiskVedleggService.delete(journalpostId, dokumentId, logiskVedleggId);
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
		hasText(journalpostId, feltnavn);
		convertStringToLong(journalpostId, feltnavn);
	}

}
