package no.nav.dokarkiv.hentjournalsakinfo;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark920.SafHentDokumentResponse;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark920.SafHentDokumentService;
import no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz.HentJournalpostListeRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz.HentJournalpostListeResponseTo;
import no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz.HentJournalpostListeService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@RestController
@RequestMapping("/hentjournalsakinfo")
public class HentJournalsakinfoController {

	private final HentJournalpostListeService hentJournalpostListeService;
	private final SafHentDokumentService safHentDokumentService;

	public HentJournalsakinfoController(HentJournalpostListeService hentJournalpostListeService,
										SafHentDokumentService safHentDokumentService) {
		this.hentJournalpostListeService = hentJournalpostListeService;
		this.safHentDokumentService = safHentDokumentService;
	}

	@Transactional(readOnly = true)
	@ResponseBody
	@PostMapping(value = "/hentjournalposter")
	public HentJournalpostListeResponseTo hentJournalposter(@RequestBody HentJournalpostListeRequestTo hentJournalpostListeRequestTo) {
		log.info("tjoarkxyz har mottatt forespørsel");
		return hentJournalpostListeService.hentJournalpostListeByArkivIdAndFagsystem(hentJournalpostListeRequestTo);
	}

	@ResponseBody
	@RequestMapping(value = "/hentdokument/{dokumentinfoId}/{variant}")
	public ResponseEntity<Base64> safHentDokument(@PathVariable(value = "dokumentinfoId") String dokumentinfoId,
												  @PathVariable(value = "variant") String variant) {
		log.info("rjoark920 har mottatt forespørsel");

		SafHentDokumentResponse safHentDokumentResponse = safHentDokumentService.hentDokumentByDokumentinfoIdAndVariant(dokumentinfoId, variant);

		return ResponseEntity.ok()
				.header(safHentDokumentResponse.getFiltype()) //mapping her
				.body(safHentDokumentResponse.getDokument());
	}
}
