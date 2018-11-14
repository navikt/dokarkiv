package no.nav.dokarkiv.hentjournalsakinfo;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.TilgangJournalpostBulkRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.TilgangJournalpostBulkResponseTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.TilgangJournalpostBulkService;
import no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz.HentJournalpostListeRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz.HentJournalpostListeResponseTo;
import no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz.HentJournalpostListeService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@RestController
@RequestMapping("/hentjournalsakinfo")
public class HentJournalsakinfoController {

	private final HentJournalpostListeService hentJournalpostListeService;
	private final TilgangJournalpostBulkService tilgangJournalpostBulkService;

	public HentJournalsakinfoController(HentJournalpostListeService hentJournalpostListeService,
										TilgangJournalpostBulkService tilgangJournalpostBulkService) {
		this.hentJournalpostListeService = hentJournalpostListeService;
		this.tilgangJournalpostBulkService = tilgangJournalpostBulkService;
	}

	@Transactional(readOnly = true)
	@ResponseBody
	@PostMapping(value = "/hentjournalposter")
	public HentJournalpostListeResponseTo hentJournalposter(@RequestBody HentJournalpostListeRequestTo hentJournalpostListeRequestTo) {
		log.info("tjoarkxyz har mottatt forespørsel");
		return hentJournalpostListeService.hentJournalpostListeByArkivIdAndFagsystem(hentJournalpostListeRequestTo);
	}

	@Transactional(readOnly = true)
	@ResponseBody
	@PostMapping(value = "/tilgangjournalpostbulk")
	public TilgangJournalpostBulkResponseTo hentJournalposter(@RequestBody TilgangJournalpostBulkRequestTo tilgangJournalpostBulkRequestTo) {
		log.info("rjoark900 henter tilgangjournalpost.");
		return tilgangJournalpostBulkService.tilgangJournalpostBulk(tilgangJournalpostBulkRequestTo);
	}
}
