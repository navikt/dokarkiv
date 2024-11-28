package no.nav.dokarkiv.hentjournalsakinfo;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.FinnJournalposterRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.FinnJournalposterResponseTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.FinnJournalposterService;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Unprotected
@Validated
@RestController
@RequestMapping("/hentjournalsakinfo")
public class HentJournalsakinfoController {

	private final FinnJournalposterService finnJournalposterService;

	public HentJournalsakinfoController(FinnJournalposterService finnJournalposterService) {
		this.finnJournalposterService = finnJournalposterService;
	}

	@Transactional(readOnly = true)
	@PostMapping(value = "/finnjournalposter")
		public FinnJournalposterResponseTo finnJournalposter(@RequestBody FinnJournalposterRequestTo finnJournalposterRequestTo) {

		List<String> gsakSakIds = finnJournalposterRequestTo.getGsakSakIds();
		List<String> psakSakIds = finnJournalposterRequestTo.getPsakSakIds();
		log.info("rjoark900 finner journalposter med antall_gsak_ids={}, antall_psak_ids={}, request={}.",
				gsakSakIds == null ? 0 : gsakSakIds.size(),
				psakSakIds == null ? 0 : psakSakIds.size(),
				finnJournalposterRequestTo);

		FinnJournalposterResponseTo finnJournalposterResponseTo = finnJournalposterService.finnJournalposter(finnJournalposterRequestTo);
		log.info("rjoark900 fant og returnerer {} journalposter med request={}.", finnJournalposterResponseTo.getTilgangJournalposter().size(), finnJournalposterRequestTo);

		return finnJournalposterResponseTo;
	}
}
