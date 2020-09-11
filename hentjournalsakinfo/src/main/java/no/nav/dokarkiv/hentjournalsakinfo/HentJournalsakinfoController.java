package no.nav.dokarkiv.hentjournalsakinfo;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.dokumenturl.MimeTypeMapper;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.FinnJournalposterRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.FinnJournalposterResponseTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.FinnJournalposterService;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark901.HentTilgangJournalpostResponse;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark901.HentTilgangJournalpostService;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark902.SafHentJournalpostResponse;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark902.SafHentJournalpostService;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark903.Tilknytning;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark903.TilknyttedeJournalposterResponse;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark903.TilknyttedeJournalposterService;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark904.FinnJournalposterStatusRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark904.FinnJournalposterStatusResponseTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark904.FinnJournalposterStatusService;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark910.DokumentoversiktBrukerRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark910.DokumentoversiktBrukerResponseTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark910.DokumentoversiktBrukerService;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark920.SafHentDokumentResponse;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark920.SafHentDokumentService;
import no.nav.security.token.support.core.api.Unprotected;
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
@Unprotected
@RestController
@RequestMapping("/hentjournalsakinfo")
public class HentJournalsakinfoController {
	public static final String RJOARK_920 = "rjoark920";
	private final SafHentDokumentService safHentDokumentService;
	private final SafHentJournalpostService safHentJournalpostService;
	private final MimeTypeMapper mimeTypeMapper = new MimeTypeMapper();
	private final FinnJournalposterService finnJournalposterService;
	private final DokumentoversiktBrukerService dokumentoversiktBrukerService;
	private final FinnJournalposterStatusService finnJournalposterStatusService;
	private final HentTilgangJournalpostService hentTilgangJournalpostService;
	private final TilknyttedeJournalposterService tilknyttedeJournalposterService;

	@Inject
	public HentJournalsakinfoController(SafHentDokumentService safHentDokumentService,
										SafHentJournalpostService safHentJournalpostService,
										FinnJournalposterService finnJournalposterService,
										DokumentoversiktBrukerService dokumentoversiktBrukerService,
										FinnJournalposterStatusService finnJournalposterStatusService,
										HentTilgangJournalpostService hentTilgangJournalpostService,
										TilknyttedeJournalposterService tilknyttedeJournalposterService) {
		this.safHentDokumentService = safHentDokumentService;
		this.safHentJournalpostService = safHentJournalpostService;
		this.finnJournalposterService = finnJournalposterService;
		this.dokumentoversiktBrukerService = dokumentoversiktBrukerService;
		this.finnJournalposterStatusService = finnJournalposterStatusService;
		this.hentTilgangJournalpostService = hentTilgangJournalpostService;
		this.tilknyttedeJournalposterService = tilknyttedeJournalposterService;
	}

	@Transactional(readOnly = true)
	@ResponseBody
	@PostMapping(value = "/finnjournalposter")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark900"}, percentiles = {0.5, 0.95})
	public FinnJournalposterResponseTo finnJournalposter(@RequestBody FinnJournalposterRequestTo finnJournalposterRequestTo) {
		log.info("rjoark900 finner journalposter med request={}.", finnJournalposterRequestTo);
		FinnJournalposterResponseTo finnJournalposterResponseTo = finnJournalposterService.finnJournalposter(finnJournalposterRequestTo);
		log.info("rjoark900 fant og returnerer {} journalposter med request={}.", finnJournalposterResponseTo.getTilgangJournalposter().size(), finnJournalposterRequestTo);
		return finnJournalposterResponseTo;
	}

	@Transactional(readOnly = true)
	@ResponseBody
	@PostMapping(value = "/dokumentoversiktbruker")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark910"}, percentiles = {0.5, 0.95})
	public DokumentoversiktBrukerResponseTo dokumentoversiktBruker(@RequestBody DokumentoversiktBrukerRequestTo dokumentoversiktBrukerRequestTo) {
		log.info("rjoark910 henter dokumentoversikt til bruker for request={}.", dokumentoversiktBrukerRequestTo);
		DokumentoversiktBrukerResponseTo dokumentoversiktBrukerResponseTo = dokumentoversiktBrukerService.hentDokumentoversiktBruker(dokumentoversiktBrukerRequestTo);
		log.info("rjoark910 fant og returnerer {} journalposter med request={}.", dokumentoversiktBrukerResponseTo.getJournalposter().size(), dokumentoversiktBrukerRequestTo);
		return dokumentoversiktBrukerResponseTo;
	}

	@Transactional(readOnly = true)
	@ResponseBody
	@PostMapping(value = "/finnjournalposterstatus")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark904"}, percentiles = {0.5, 0.95})
	public FinnJournalposterStatusResponseTo finnJournalposterStatus(@RequestBody FinnJournalposterStatusRequestTo finnJournalposterStatusRequestTo) {
		log.info("rjoark904 finner journalposter med request={}.", finnJournalposterStatusRequestTo);
		FinnJournalposterStatusResponseTo finnJournalposterStatusResponseTo = finnJournalposterStatusService.finnJournalposterStatus(finnJournalposterStatusRequestTo);
		log.info("rjoark904 fant og returnerer {} journalposter med request={}.", finnJournalposterStatusResponseTo.getTilgangJournalposter().size(), finnJournalposterStatusRequestTo);
		return finnJournalposterStatusResponseTo;
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
	@RequestMapping(value = "/hentjournalpost/{journalpostId}")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark902"}, percentiles = {0.5, 0.95})
	public SafHentJournalpostResponse safHentJournalpost(@PathVariable Long journalpostId) {
		log.info("rjoark902 har mottatt forespørsel om journalpost med journalpostId={}", journalpostId);
		return safHentJournalpostService.hentJournalpostByJournalpostId(journalpostId);
	}

	@Transactional(readOnly = true)
	@ResponseBody
	@RequestMapping(value = "/tilknyttedejournalposter/{dokumentInfoId}/{tilknytning}")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark903"}, percentiles = {0.5, 0.95})
	public TilknyttedeJournalposterResponse tilknyttedeJournalposter(@PathVariable Long dokumentInfoId,
																	 @PathVariable Tilknytning tilknytning) {
		log.info("rjoark903 har mottatt forespørsel om tilknyttede journalposter for dokumentInfoId={} med tilknytning={}", dokumentInfoId, tilknytning);
		return tilknyttedeJournalposterService.tilknyttedeJournalposter(dokumentInfoId, tilknytning);
	}

	@Transactional(readOnly = true)
	@RequestMapping(value = "/hentdokument/{dokumentinfoId}/{variant}")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", RJOARK_920}, percentiles = {0.5, 0.95})
	public ResponseEntity<String> safHentDokument(@PathVariable Long dokumentinfoId,
												  @PathVariable VariantFormatCode variant) {
		RequestContextUtil.createAndSetUsername(RJOARK_920, "dokarkiv");
		log.info("rjoark920 har mottatt forespørsel om dokument med dokumentinfoId={} og variant={}", dokumentinfoId, variant);
		SafHentDokumentResponse safHentDokumentResponse = safHentDokumentService.hentDokumentByDokumentinfoIdAndVariant(dokumentinfoId, variant);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_TYPE, mimeTypeMapper.getMimeTypeForFileExtension(safHentDokumentResponse.getFiltype().toString()))
				.body(Base64.getEncoder().encodeToString(safHentDokumentResponse.getDokument()));
	}
}
