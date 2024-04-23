package no.nav.dokarkiv.safintern.journalpost;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.safintern.views.JournalpostView;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.safintern.SafinternConstants.BASE_PATH;
import static no.nav.dokarkiv.safintern.SafinternConstants.ROLE_CLAIM_TILGANG;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(BASE_PATH)
@ProtectedWithClaims(issuer = ISSUER_AZUREV2, claimMap = {"roles=" + ROLE_CLAIM_TILGANG})
public class JournalpostController {

	private final SafinternJournalpostService safinternJournalpostService;

	public JournalpostController(SafinternJournalpostService safinternJournalpostService) {
		this.safinternJournalpostService = safinternJournalpostService;
	}

	@GetMapping(value = "/journalpost/journalpostId/{journalpostId}", produces = APPLICATION_JSON_VALUE)
	public JournalpostView hentJournalpostById(@PathVariable final Long journalpostId,
											   @RequestParam(required = false) Set<String> fields) {
		log.info("safintern/journalpost har mottatt kall om journalpost med journalpostId={}, fields={}", journalpostId, fields);
		JournalpostView journalpostView = safinternJournalpostService.hentJournalpostById(journalpostId, fields);
		log.info("safintern/journalpost hentet journalpost med journalpostId={}", journalpostId);
		return journalpostView;
	}

	@GetMapping(value = "/journalpost/journalpostId/{journalpostId}/dokumentInfoId/{dokumentInfoId}", produces = APPLICATION_JSON_VALUE)
	public JournalpostView hentJournalpostById(@PathVariable final Long journalpostId,
											   @PathVariable final Long dokumentInfoId,
											   @RequestParam(required = false) Set<String> fields) {
		log.info("safintern/journalpost har mottatt kall om journalpost med journalpostId={}, dokumentInfoId={}, fields={}", journalpostId, dokumentInfoId, fields);
		JournalpostView journalpostView = safinternJournalpostService.hentJournalpostByIdAndDokumentInfoId(journalpostId, dokumentInfoId, fields);
		log.info("safintern/journalpost hentet journalpost med journalpostId={}, dokumentInfoId={}", journalpostId, dokumentInfoId);
		return journalpostView;
	}

	@GetMapping(value = "/journalpost/eksternReferanseId/{eksternReferanseId}", produces = APPLICATION_JSON_VALUE)
	public JournalpostView hentJournalpostByEksternReferanseId(@PathVariable final String eksternReferanseId,
															   @RequestParam(required = false) Set<String> fields) {
		log.info("safintern/journalpost har mottatt kall om journalpost med eksternReferanseId={}, fields={}", eksternReferanseId, fields);
		JournalpostView journalpostView = safinternJournalpostService.hentJournalpostByEksternReferanseId(eksternReferanseId, fields);
		log.info("safintern/journalpost hentet journalpost med eksternReferanseId={}", eksternReferanseId);
		return journalpostView;
	}

	@GetMapping(value = "/tilknyttedeJournalposter/gjenbruk/dokumentInfoId/{dokumentInfoId}", produces = APPLICATION_JSON_VALUE)
	public List<JournalpostView> hentJournalpostTilknyttetGjenbruk(@PathVariable Long dokumentInfoId,
														   @RequestParam(required = false) Set<String> fields) {
		log.info("safintern/tilknyttedeJournalposter har mottatt kall om journalposter tiilknyttet dokumentInfoId={}, fields={}", dokumentInfoId, fields);
		List<JournalpostView> journalpostsView = safinternJournalpostService.hentJournalposterTilknyttetGjenbruk(dokumentInfoId, fields);
		log.info("safintern/tilknyttedeJournalposter hentet journalposter tilknyttet dokumentInfoId={}", dokumentInfoId);
		return journalpostsView;
	}
}
