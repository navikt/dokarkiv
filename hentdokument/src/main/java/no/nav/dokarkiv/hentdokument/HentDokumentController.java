package no.nav.dokarkiv.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.dokumenturl.MimeTypeMapper;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.hentdokument.dokument.HentDokument;
import no.nav.dokarkiv.hentdokument.dokument.HentDokumentRequest;
import no.nav.dokarkiv.hentdokument.dokument.HentDokumentResponse;
import no.nav.dokarkiv.hentdokument.dokumenturlinfo.HentDokumentUrlInfo;
import no.nav.dokarkiv.hentdokument.dokumenturlinfo.HentDokumentUrlInfoRequest;
import no.nav.dokarkiv.hentdokument.dokumenturlinfo.HentDokumentUrlInfoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@Slf4j
@RestController
public class HentDokumentController {

	private final HentDokumentUrlInfo hentDokumentUrlInfo;
	private final HentDokument hentDokument;

	private final MimeTypeMapper mimeTypeMapper = new MimeTypeMapper();

	@Inject
	public HentDokumentController(HentDokumentUrlInfo hentDokumentUrlInfo, HentDokument hentDokument) {
		this.hentDokumentUrlInfo = hentDokumentUrlInfo;
		this.hentDokument = hentDokument;
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "hentdokument")
	public ResponseEntity getDokument(final @RequestParam("docToken") String docToken) {
		RequestContextUtil.createAndSetUsername("hentdokument", "dokarkiv");
		try {
			DokumentUrlInfo dokUrlInfo = getDokumentUrlInfo(docToken);
			log.info("hentdokument henter dokument med docToken={}", docToken);
			Long journalpostId = dokUrlInfo.getJournalpost().getJournalpostId();
			String filUuid = dokUrlInfo.getFilUuid();

			byte[] document = getDocument(journalpostId, filUuid, docToken);

			FilTypeCode filtype = getFilTypeFromJournalpost(dokUrlInfo.getJournalpost(), filUuid);
			String mediaType = getContentType(filtype);

			return ResponseEntity
					.ok()
					.contentType(MediaType.parseMediaType(mediaType))
					.contentLength(document.length)
					.body(document);
		} catch (DokarkivFunctionalException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	private DokumentUrlInfo getDokumentUrlInfo(String docToken) {
		HentDokumentUrlInfoResponse hentDokumentUrlInfoResponse = hentDokumentUrlInfo.hentDokumentUrlInfo(new HentDokumentUrlInfoRequest(docToken));
		return hentDokumentUrlInfoResponse.getDokumentUrl();
	}

	private byte[] getDocument(final Long journalpostId, final String filUuid, final String docToken) throws NoJournalpostFoundException,
			InvalidFilUuidException {
		HentDokumentRequest hentDokumentRequest = new HentDokumentRequest(journalpostId, filUuid, docToken);
		HentDokumentResponse hentDokumentResponse = hentDokument.hentDokument(hentDokumentRequest);
		return hentDokumentResponse.getDokument();
	}

	private FilTypeCode getFilTypeFromJournalpost(Journalpost journalpost, String filUuid) {
		FilDetaljer filDetaljer = journalpost.findFilDetaljerByFilUuid(filUuid);
		return filDetaljer == null ? null : filDetaljer.getFiltype();
	}

	private String getContentType(FilTypeCode filtype) {
		return mimeTypeMapper.getMimeTypeForFileExtension(filtype == null ? "" : filtype.name());
	}

}