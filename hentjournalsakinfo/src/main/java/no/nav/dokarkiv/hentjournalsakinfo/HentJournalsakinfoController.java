package no.nav.dokarkiv.hentjournalsakinfo;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.hentjournalsakinfo.dto.TilgangJournalpostDto;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark901.HentTilgangJournalpostService;
import no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz.HentJournalpostListeRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz.HentJournalpostListeResponseTo;
import no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz.HentJournalpostListeService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	private final HentTilgangJournalpostService hentTilgangJournalpostService;

	public HentJournalsakinfoController(HentJournalpostListeService hentJournalpostListeService,
										HentTilgangJournalpostService hentTilgangJournalpostService) {
		this.hentJournalpostListeService = hentJournalpostListeService;
		this.hentTilgangJournalpostService = hentTilgangJournalpostService;
	}

	@Transactional(readOnly = true)
	@ResponseBody
	@PostMapping(value = "/hentjournalposter")
	public HentJournalpostListeResponseTo hentJournalposter(@RequestBody HentJournalpostListeRequestTo hentJournalpostListeRequestTo) {
		log.info("tjoarkxyz har mottatt forespørsel");
		return hentJournalpostListeService.hentJournalpostListeByArkivIdAndFagsystem(hentJournalpostListeRequestTo);
	}


	@ResponseBody
	@GetMapping(value = "/henttilgangjournalpost/{journalpostId}/{dokumentInfoId}/{variantFormat}")
	public TilgangJournalpostDto hentTilgangJournalpost(@PathVariable Long journalpostId,
														@PathVariable Long dokumentInfoId,
														@PathVariable VariantFormatCode variantFormat) {
		log.info("rjoark901 har mottatt forespørsel om å hente TilgangJournalpost for journalpost med journalpostId={}, dokumentInfoId={} og variantFormat={}",
				journalpostId, dokumentInfoId, variantFormat.name());
		return hentTilgangJournalpostService.hentTilgangJournalpost(journalpostId, dokumentInfoId, variantFormat);
	}

}
