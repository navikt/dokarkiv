package no.nav.dokarkiv.hentjournalsakinfo;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.FinnJournalposterRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.FinnJournalposterResponseTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.FinnJournalposterService;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark903.Tilknytning;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark903.TilknyttedeJournalposterResponse;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark903.TilknyttedeJournalposterService;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark904.FinnJournalposterStatusRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark904.FinnJournalposterStatusResponseTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark904.FinnJournalposterStatusService;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
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
	private final FinnJournalposterStatusService finnJournalposterStatusService;
	private final TilknyttedeJournalposterService tilknyttedeJournalposterService;

	public HentJournalsakinfoController(FinnJournalposterService finnJournalposterService,
										FinnJournalposterStatusService finnJournalposterStatusService,
										TilknyttedeJournalposterService tilknyttedeJournalposterService) {
		this.finnJournalposterService = finnJournalposterService;
		this.finnJournalposterStatusService = finnJournalposterStatusService;
		this.tilknyttedeJournalposterService = tilknyttedeJournalposterService;
	}

	@Transactional(readOnly = true)
	@PostMapping(value = "/finnjournalposter")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark900"}, percentiles = {0.5, 0.95})
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

	@Transactional(readOnly = true)
	@RequestMapping(value = "/tilknyttedejournalposter/{dokumentInfoId}/{tilknytning}")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark903"}, percentiles = {0.5, 0.95})
	public TilknyttedeJournalposterResponse tilknyttedeJournalposter(@PathVariable Long dokumentInfoId,
																	 @PathVariable Tilknytning tilknytning) {
		log.info("rjoark903 har mottatt forespørsel om tilknyttede journalposter for dokumentInfoId={} med tilknytning={}", dokumentInfoId, tilknytning);
		return tilknyttedeJournalposterService.tilknyttedeJournalposter(dokumentInfoId, tilknytning);
	}

	@Transactional(readOnly = true)
	@PostMapping(value = "/finnjournalposterstatus")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark904"}, percentiles = {0.5, 0.95})
	public FinnJournalposterStatusResponseTo finnJournalposterStatus(@RequestBody FinnJournalposterStatusRequestTo finnJournalposterStatusRequestTo) {

		log.info("rjoark904 finner journalposter med request={}.", finnJournalposterStatusRequestTo);
		FinnJournalposterStatusResponseTo finnJournalposterStatusResponseTo = finnJournalposterStatusService.finnJournalposterStatus(finnJournalposterStatusRequestTo);
		log.info("rjoark904 fant og returnerer {} journalposter med request={}.", finnJournalposterStatusResponseTo.getTilgangJournalposter().size(), finnJournalposterStatusRequestTo);
		return finnJournalposterStatusResponseTo;
	}
}
