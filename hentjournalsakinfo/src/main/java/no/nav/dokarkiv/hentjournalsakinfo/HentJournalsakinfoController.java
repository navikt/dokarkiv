package no.nav.dokarkiv.hentjournalsakinfo;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.dokumenturl.MimeTypeMapper;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
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
	public ResponseEntity<String> safHentDokument(@PathVariable(value = "dokumentinfoId") Long dokumentinfoId,
												  @PathVariable(value = "variant") VariantFormatCode variant) {

		log.info(String.format("rjoark920 har mottatt forespørsel om dokument med dokumentinfoId=%s og variant=%s", dokumentinfoId, variant));
		SafHentDokumentResponse safHentDokumentResponse = safHentDokumentService.hentDokumentByDokumentinfoIdAndVariant(dokumentinfoId, variant);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_TYPE, mimeTypeMapper.getMimeTypeForFileExtension(safHentDokumentResponse.getFiltype().toString()))
				.body(Base64.getEncoder().encodeToString(safHentDokumentResponse.getDokument()));
	}


}
