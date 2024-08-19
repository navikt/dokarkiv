package no.nav.dokarkiv.safintern.journalstatus;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.safintern.views.PaginatedJournalpostView;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
public class FinnJournalposterStatusController {

	private final SafinternJournalStatusService safinternJournalStatusService;

	public FinnJournalposterStatusController(SafinternJournalStatusService safinternJournalStatusService) {
		this.safinternJournalStatusService = safinternJournalStatusService;
	}

	@PostMapping(value = "finnjournalposterstatus", produces = APPLICATION_JSON_VALUE)
	public PaginatedJournalpostView finnJournalposterStatus(@RequestBody FinnJournalposterStatusRequest finnJournalposterStatusRequest,
															@RequestParam(required = false) Set<String> fields) {
		JournalStatusCode journalstatus = finnJournalposterStatusRequest.journalstatus();
		List<JournalpostTypeCode> journalposttyper = finnJournalposterStatusRequest.journalposttyper();
		log.info("safintern/finnjournalposterstatus har mottatt kall om journalposter med status={}, typer={}, fields={}", journalstatus, journalposttyper, fields);
		var journalpostsView = safinternJournalStatusService.finnJournalposterStatus(finnJournalposterStatusRequest, fields);
		log.info("safintern/finnjournalposterstatus hentet journalposter med status={}. typer={}, side {} av {}", journalstatus, journalposttyper, journalpostsView.page(), journalpostsView.totalPages());
		return journalpostsView;
	}
}
