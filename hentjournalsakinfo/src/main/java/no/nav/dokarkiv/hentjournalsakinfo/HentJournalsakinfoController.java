package no.nav.dokarkiv.hentjournalsakinfo;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.dokumenturl.MimeTypeMapper;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.HentJournalpostBulkRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.HentJournalpostBulkResponseTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.HentJournalpostBulkService;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark910.VisningJournalpostBulkRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark910.VisningJournalpostBulkResponseTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark910.VisningJournalpostBulkService;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark920.SafHentDokumentResponse;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark920.SafHentDokumentService;
import no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz.HentJournalpostListeRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz.HentJournalpostListeResponseTo;
import no.nav.dokarkiv.hentjournalsakinfo.tjoarkxyz.HentJournalpostListeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
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
	private final MimeTypeMapper mimeTypeMapper = new MimeTypeMapper();
	private final HentJournalpostBulkService hentJournalpostBulkService;
	private final VisningJournalpostBulkService visningJournalpostBulkService;

	@Inject
	public HentJournalsakinfoController(HentJournalpostListeService hentJournalpostListeService,
										SafHentDokumentService safHentDokumentService,
										HentJournalpostBulkService hentJournalpostBulkService,
										VisningJournalpostBulkService visningJournalpostBulkService) {
		this.hentJournalpostListeService = hentJournalpostListeService;
		this.safHentDokumentService = safHentDokumentService;
		this.hentJournalpostBulkService = hentJournalpostBulkService;
		this.visningJournalpostBulkService = visningJournalpostBulkService;
	}

	@Transactional(readOnly = true)
	@PostMapping(value = "/hentjournalposter")
	public HentJournalpostListeResponseTo hentJournalposter(@RequestBody HentJournalpostListeRequestTo hentJournalpostListeRequestTo) {
		log.info("tjoarkxyz har mottatt forespørsel");
		return hentJournalpostListeService.hentJournalpostListeByArkivIdAndFagsystem(hentJournalpostListeRequestTo);
	}

	@Transactional(readOnly = true)
	@ResponseBody
	@RequestMapping(value = "/hentdokument/{dokumentinfoId}/{variant}")
	public ResponseEntity<String> safHentDokument(@PathVariable Long dokumentinfoId,
												  @PathVariable VariantFormatCode variant) {

		log.info("rjoark920 har mottatt forespørsel om dokument med dokumentinfoId={} og variant={}", dokumentinfoId, variant);
		SafHentDokumentResponse safHentDokumentResponse = safHentDokumentService.hentDokumentByDokumentinfoIdAndVariant(dokumentinfoId, variant);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_TYPE, mimeTypeMapper.getMimeTypeForFileExtension(safHentDokumentResponse.getFiltype().toString()))
				.body(Base64.getEncoder().encodeToString(safHentDokumentResponse.getDokument()));
	}


	@Transactional(readOnly = true)
	@ResponseBody
	@PostMapping(value = "/hentjournalpostbulk")
	public HentJournalpostBulkResponseTo hentJournalpostBulk(@RequestBody HentJournalpostBulkRequestTo hentJournalpostBulkRequestTo) {
		log.info("rjoark900 henter journalpostbulk.");
		return hentJournalpostBulkService.hentJournalpostBulk(hentJournalpostBulkRequestTo);
	}

	@Transactional(readOnly = true)
	@ResponseBody
	@PostMapping(value = "/visningjournalpostbulk")
	public VisningJournalpostBulkResponseTo visningsJournalpostBulk(@RequestBody VisningJournalpostBulkRequestTo visningJournalpostBulkRequestTo) {
		log.info("rjoark910 henter journalposter.");
		return visningJournalpostBulkService.visningJournalpostBulk(visningJournalpostBulkRequestTo);
	}
}
