package no.nav.dokarkiv.safintern.finnjournalposter;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.safintern.views.PaginatedJournalpostView;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.core.util.SafeLoggingUtil.removeUnsafeChars;
import static no.nav.dokarkiv.safintern.SafinternConstants.BASE_PATH;
import static no.nav.dokarkiv.safintern.SafinternConstants.ROLE_CLAIM_TILGANG;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(BASE_PATH)
@ProtectedWithClaims(issuer = ISSUER_AZUREV2, claimMap = {"roles=" + ROLE_CLAIM_TILGANG})
public class FinnJournalposterController {

	private final SafinternFinnJournalposterService safinternFinnJournalposterService;

	public FinnJournalposterController(SafinternFinnJournalposterService safinternFinnJournalposterService) {
		this.safinternFinnJournalposterService = safinternFinnJournalposterService;
	}

	@PostMapping(value = "finnjournalposter", produces = APPLICATION_JSON_VALUE)
	public PaginatedJournalpostView finnJournalposter(@RequestBody FinnJournalposterRequest finnJournalposterRequest,
													  @RequestParam(required = false) Set<String> fields) {
		log.info("safintern/finnjournalposter har mottatt kall om journalposter med statuser={}, typer={}," +
						" fraDato={}, tilDato={}, visFeilregistrerte={}, psakSakIds={}, gsakSakIds={}, fields={}",
				finnJournalposterRequest.journalstatuser(), finnJournalposterRequest.journalposttyper(),
				finnJournalposterRequest.fraDato(), finnJournalposterRequest.tilDato(), finnJournalposterRequest.visFeilregistrerte(),
				finnJournalposterRequest.psakSakIds(), finnJournalposterRequest.gsakSakIds(), removeUnsafeChars(fields));
		
		var journalpostsView = safinternFinnJournalposterService.finnJournalposter(finnJournalposterRequest, fields);
		log.info("safintern/finnjournalposter hentet journalposter med side {} av {}", journalpostsView.page(), journalpostsView.totalPages());
		return journalpostsView;
	}
}
