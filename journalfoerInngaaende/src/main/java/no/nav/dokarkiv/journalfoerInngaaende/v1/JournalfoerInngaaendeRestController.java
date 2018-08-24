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
import no.nav.dok.tjenester.journalfoerinngaaende.PutDokumentRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutDokumentResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostResponse;
import no.nav.dokarkiv.core.exceptions.DokarkivRestFunctionalException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.security.abac.AuthorizationException;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalfoerInngaaende.v1.service.GetInngaaendeJournalpostService;
import no.nav.dokarkiv.journalfoerInngaaende.v1.service.PersistInngaaendeJournalpostService;
import no.nav.dokarkiv.journalfoerInngaaende.v1.service.UpdateInngaaendeJournalpostDokumentService;
import no.nav.freg.abac.core.annotation.Abac;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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

	private final GetInngaaendeJournalpostService getInngaaendeJournalpostService;
	private final PersistInngaaendeJournalpostService persistInngaaendeJournalpostService;
	private final UpdateInngaaendeJournalpostDokumentService updateInngaaendeJournalpostDokumentService;
	private final AbacSecurityService abacSecurityService;

	@Inject
	public JournalfoerInngaaendeRestController(GetInngaaendeJournalpostService getInngaaendeJournalpostService,
											   PersistInngaaendeJournalpostService persistInngaaendeJournalpostService,
											   UpdateInngaaendeJournalpostDokumentService updateInngaaendeJournalpostDokumentService,
											   AbacSecurityService abacSecurityService) {
		this.getInngaaendeJournalpostService = getInngaaendeJournalpostService;
		this.abacSecurityService = abacSecurityService;
		this.updateInngaaendeJournalpostDokumentService = updateInngaaendeJournalpostDokumentService;
		this.persistInngaaendeJournalpostService = persistInngaaendeJournalpostService;
	}

	@GetMapping("/{journalpostId}")
	@Transactional(readOnly = true)
	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)})
	public ResponseEntity getInngaaendeJournalpostByJournalpostId(@PathVariable String journalpostId) {
		try {
			validateJournalpostId(journalpostId);
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
			validateJournalpostId(journalpostId);
			assertAccessToJournalpost(journalpostId);
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

	@PutMapping(value = "/{journalpostId}/dokumenter/{dokumentid}")
	@Transactional
	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)})
	public ResponseEntity updateDokument(@PathVariable String journalpostId, @PathVariable String dokumentid, @RequestBody PutDokumentRequest request) {
		try {

			RequestContextUtil.createAndSetUsername("user", "appid");
			PutDokumentResponse inngaaendeResponseTo = updateInngaaendeJournalpostDokumentService.update(journalpostId, dokumentid, request);
			log.info("Oppdatert dokument med journalpostId={} og dokumentId={} i Joark.", journalpostId, dokumentid);
			return new ResponseEntity<>(inngaaendeResponseTo, HttpStatus.OK);
		} catch (DokarkivRestFunctionalException e) {
			log.warn("Feilmelding={}, HttpStatus={}", e.getMessage(), e
					.getHttpStatus());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_PLAIN);
			return new ResponseEntity<>(e.getMessage(), headers, e.getHttpStatus());
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

	private void validateJournalpostId(String journalpostId) {
		hasText(journalpostId, "journalpostId");
		convertStringToLong(journalpostId, "journalpostId");
	}

}
