package no.nav.dokarkiv.hentjournalsakinfo;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.dokumenturl.MimeTypeMapper;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.FinnJournalposterRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.FinnJournalposterResponseTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.HentJournalpostBulkService;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark901.HentTilgangJournalpostResponse;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark901.HentTilgangJournalpostService;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark910.VisningJournalpostBulkRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark910.VisningJournalpostBulkResponseTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark910.VisningJournalpostBulkService;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark920.SafHentDokumentResponse;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark920.SafHentDokumentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
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
	private final SafHentDokumentService safHentDokumentService;
	private final MimeTypeMapper mimeTypeMapper = new MimeTypeMapper();
	private final HentJournalpostBulkService hentJournalpostBulkService;
	private final VisningJournalpostBulkService visningJournalpostBulkService;
	private final HentTilgangJournalpostService hentTilgangJournalpostService;

	@Inject
	public HentJournalsakinfoController(SafHentDokumentService safHentDokumentService,
										HentJournalpostBulkService hentJournalpostBulkService,
										VisningJournalpostBulkService visningJournalpostBulkService,
										HentTilgangJournalpostService hentTilgangJournalpostService) {
		this.safHentDokumentService = safHentDokumentService;
		this.hentJournalpostBulkService = hentJournalpostBulkService;
		this.visningJournalpostBulkService = visningJournalpostBulkService;
		this.hentTilgangJournalpostService = hentTilgangJournalpostService;

	}

	@Transactional(readOnly = true)
	@ResponseBody
	@PostMapping(value = "/finnjournalposter")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark900"}, percentiles = {0.5, 0.95})
	public FinnJournalposterResponseTo finnJournalposter(@RequestBody FinnJournalposterRequestTo finnJournalposterRequestTo) {
		log.info("rjoark900 finner journalposter.");
		FinnJournalposterResponseTo finnJournalposterResponseTo = hentJournalpostBulkService.hentJournalpostBulk(finnJournalposterRequestTo);
		log.info("rjoark900 fant og returnerer {} journalposter.", finnJournalposterResponseTo.getTilgangJournalposter().size());
		return finnJournalposterResponseTo;
	}

	@Transactional(readOnly = true)
	@ResponseBody
	@GetMapping(value = "/henttilgangjournalpost/{journalpostId}/{dokumentInfoId}/{variantFormat}")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "tjoark901"}, percentiles = {0.5, 0.95})
	public HentTilgangJournalpostResponse hentTilgangJournalpost(@PathVariable Long journalpostId,
																 @PathVariable Long dokumentInfoId,
																 @PathVariable VariantFormatCode variantFormat) {
		log.info("rjoark901 har mottatt forespørsel om å hente TilgangJournalpost for journalpost med journalpostId={}, dokumentInfoId={} og variantFormat={}",
				journalpostId, dokumentInfoId, variantFormat.name());
		return hentTilgangJournalpostService.hentTilgangJournalpost(journalpostId, dokumentInfoId, variantFormat);
	}

	@Transactional(readOnly = true)
	@ResponseBody
	@PostMapping(value = "/hentjournalposter")
	public VisningJournalpostBulkResponseTo visningsJournalpostBulk(@RequestBody VisningJournalpostBulkRequestTo visningJournalpostBulkRequestTo) {
		log.info("rjoark910 henter journalposter.");
		VisningJournalpostBulkResponseTo responseTo = visningJournalpostBulkService.visningJournalpostBulk(visningJournalpostBulkRequestTo);
		log.info("rjoark910 hentet {} journalposter.", responseTo.getJournalposter().size());
		return responseTo;
	}

	@Transactional(readOnly = true)
	@ResponseBody
	@RequestMapping(value = "/hentdokument/{dokumentinfoId}/{variant}")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark920"}, percentiles = {0.5, 0.95})
	public ResponseEntity<String> safHentDokument(@PathVariable Long dokumentinfoId,
												  @PathVariable VariantFormatCode variant) {
		log.info("rjoark920 har mottatt forespørsel om dokument med dokumentinfoId={} og variant={}", dokumentinfoId, variant);
		SafHentDokumentResponse safHentDokumentResponse = safHentDokumentService.hentDokumentByDokumentinfoIdAndVariant(dokumentinfoId, variant);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_TYPE, mimeTypeMapper.getMimeTypeForFileExtension(safHentDokumentResponse.getFiltype().toString()))
				.body(Base64.getEncoder().encodeToString(safHentDokumentResponse.getDokument()));
	}
}
