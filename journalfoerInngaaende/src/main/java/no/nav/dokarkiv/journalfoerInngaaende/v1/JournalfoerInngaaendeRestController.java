package no.nav.dokarkiv.journalfoerInngaaende.v1;


import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.READ_ACTION;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.Utils.getDokumentIds;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.Utils.getDokumenttypeIds;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.Utils.hasText;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.DokarkivRestFunctionalException;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.security.abac.AuthorizationException;
import no.nav.dokarkiv.journalfoerInngaaende.v1.service.GetInngaaendeJournalpostService;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.JournalpostResponseTo;
import no.nav.freg.abac.core.annotation.Abac;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;


/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@RestController
@RequestMapping("journalfoer-inngaaende/v1")
@Slf4j
public class JournalfoerInngaaendeRestController {

	private GetInngaaendeJournalpostService getInngaaendeJournalpostService;
	private AbacSecurityService abacSecurityService;

	@Inject
	public JournalfoerInngaaendeRestController(GetInngaaendeJournalpostService getInngaaendeJournalpostService,
											   AbacSecurityService abacSecurityService) {
		this.getInngaaendeJournalpostService = getInngaaendeJournalpostService;
		this.abacSecurityService = abacSecurityService;
	}

	@GetMapping(value = "/journalpost/{journalpostId}")
	@Transactional(readOnly = true)
	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)})
	public ResponseEntity getInngaaendeJournalpostByJournalpostId(@PathVariable String journalpostId) {
		try {
			hasText(journalpostId, "journalpostId");
			assertAccessToHentJournalpost(journalpostId);
			JournalpostResponseTo responseTo = getInngaaendeJournalpostService.getInngaaendeJournalpostByJournalpostId(journalpostId);
			log.info("Hentet journalpost med journalpostId={}, dokumentinfoId(er)={} og dokumenttypeId(er)={} fra Joark.",
					journalpostId, getDokumentIds(responseTo), getDokumenttypeIds(responseTo));
			return new ResponseEntity<>(responseTo, HttpStatus.OK);
		} catch (DokarkivRestFunctionalException e) {
			log.info(e.getMessage());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_PLAIN);
			return new ResponseEntity<>(e.getMessage(), headers, e.getHttpStatus());
		}
	}

	private void assertAccessToHentJournalpost(String journalpostId) throws DokarkivRestFunctionalException {
		try {
			hasText(journalpostId, "journalpostId");
			abacSecurityService.assertAccessToJournalpost(journalpostId);
		} catch (AuthorizationException e) {
			throw new DokarkivRestFunctionalException(e.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}


}
