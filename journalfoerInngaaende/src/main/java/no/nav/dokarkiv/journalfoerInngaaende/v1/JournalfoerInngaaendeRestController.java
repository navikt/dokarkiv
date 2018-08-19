package no.nav.dokarkiv.journalfoerInngaaende.v1;


import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.READ_ACTION;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.Utils.getDokumentIds;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.Utils.getDokumenttypeIds;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.Utils.hasText;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.tjenester.journalfoerinngaaende.JournalpostResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostResponse;
import no.nav.dokarkiv.core.exceptions.DokarkivRestFunctionalException;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.security.abac.AuthorizationException;
import no.nav.dokarkiv.journalfoerInngaaende.v1.service.GetInngaaendeJournalpostService;
import no.nav.dokarkiv.journalfoerInngaaende.v1.service.PersistInngaaendeJournalpostService;
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
@RequestMapping("/rest/journalfoer-inngaaende/v1")
public class JournalfoerInngaaendeRestController {

	private GetInngaaendeJournalpostService getInngaaendeJournalpostService;
	private PersistInngaaendeJournalpostService persistInngaaendeJournalpostService;
	private AbacSecurityService abacSecurityService;

	@Inject
	public JournalfoerInngaaendeRestController(GetInngaaendeJournalpostService getInngaaendeJournalpostService,
											   PersistInngaaendeJournalpostService persistInngaaendeJournalpostService,
											   AbacSecurityService abacSecurityService) {
		this.getInngaaendeJournalpostService = getInngaaendeJournalpostService;
		this.abacSecurityService = abacSecurityService;
		this.persistInngaaendeJournalpostService = persistInngaaendeJournalpostService;
	}

	@GetMapping("/journalpost/{journalpostId}")
	@Transactional(readOnly = true)
	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)})
	public ResponseEntity getInngaaendeJournalpostByJournalpostId(@PathVariable String journalpostId) {
		try {
			hasText(journalpostId, "journalpostId");
			assertAccessToJournalpost(journalpostId);
			JournalpostResponse responseTo = getInngaaendeJournalpostService.getInngaaendeJournalpostByJournalpostId(journalpostId);
			log.info("Hentet journalpost med journalpostId={}, dokumentinfoId(er)={} og dokumenttypeId(er)={} fra Joark.",
					journalpostId, getDokumentIds(responseTo), getDokumenttypeIds(responseTo));
			return new ResponseEntity<>(responseTo, HttpStatus.OK);
		} catch (DokarkivRestFunctionalException e) {
			log.warn("Feilmelding={}, journalpostId={}. HttpStatus=", e.getMessage(), journalpostId, e.getHttpStatus());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_PLAIN);
			return new ResponseEntity<>(e.getMessage(), headers, e.getHttpStatus());
		}
	}

	@PutMapping(value = "/journalpost/{journalpostId}")
	@Transactional
	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)})
	public ResponseEntity persistInngaaendeJournalpost(@PathVariable String journalpostId, @RequestBody PutJournalpostRequest request) {
		try {
			hasText(journalpostId, "journalpostId");
			assertAccessToJournalpost(journalpostId);
			PutJournalpostResponse inngaaendeResponseTo = persistInngaaendeJournalpostService.persist(journalpostId, request);
			log.info("Oppdatert journalpost med journalpostId={} i Joark.", journalpostId);
			return new ResponseEntity<>(inngaaendeResponseTo, HttpStatus.OK);
		} catch (DokarkivRestFunctionalException e) {
			log.warn("Feilmelding={}, journalpostId={}. HttpStatus=", e.getMessage(), journalpostId, e.getHttpStatus());
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
		}
	}

}
