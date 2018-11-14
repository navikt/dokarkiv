package no.nav.dokarkiv.hentjournalsakinfo;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.TilgangJournalpostBulkRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.TilgangJournalpostBulkResponseTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.TilgangJournalpostBulkService;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark910.VisningJournalpostBulkRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark910.VisningJournalpostBulkResponseTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark910.VisningJournalpostBulkService;
import no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz.HentJournalpostListeRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz.HentJournalpostListeResponseTo;
import no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz.HentJournalpostListeService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@RestController
@RequestMapping("/hentjournalsakinfo")
public class HentJournalsakinfoController {

	private final HentJournalpostListeService hentJournalpostListeService;
	private final TilgangJournalpostBulkService tilgangJournalpostBulkService;
	private final VisningJournalpostBulkService visningJournalpostBulkService;

	@Inject
	public HentJournalsakinfoController(HentJournalpostListeService hentJournalpostListeService,
										TilgangJournalpostBulkService tilgangJournalpostBulkService,
										VisningJournalpostBulkService visningJournalpostBulkService) {
		this.hentJournalpostListeService = hentJournalpostListeService;
		this.tilgangJournalpostBulkService = tilgangJournalpostBulkService;
		this.visningJournalpostBulkService = visningJournalpostBulkService;
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

	@Transactional(readOnly = true)
	@ResponseBody
	@PostMapping(value = "/visningjournalpostbulk")
	public VisningJournalpostBulkResponseTo hentJournalposter(@RequestBody VisningJournalpostBulkRequestTo visningJournalpostBulkRequestTo) {
		log.info("rjoark910 henter journalposter.");
		return visningJournalpostBulkService.visningJournalpostBulk(visningJournalpostBulkRequestTo);
	}
}
