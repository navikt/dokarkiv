package no.nav.dokarkiv.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.dokument.HentDokumentRequest;
import no.nav.dokarkiv.core.dokument.HentDokumentResponse;
import no.nav.dokarkiv.core.dokumenturl.MimeTypeMapper;
import no.nav.dokarkiv.core.dokumenturlinfo.HentDokumentUrlInfoRequest;
import no.nav.dokarkiv.core.dokumenturlinfo.HentDokumentUrlInfoResponse;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.journal.JournalServiceBi;
import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@Slf4j
@RequestMapping(value = "/dokarkiv/")
@RestController
public class HentDokumentController {

	@Inject
	private JournalServiceBi service;

	private MimeTypeMapper mimeTypeMapper = new MimeTypeMapper();

	@Transactional(readOnly = true)
	@GetMapping(value = "hentDokument")
	public ResponseEntity<byte[]> getDokument(final @RequestParam("docToken") String docToken) {
		// metrikker ?
		try {
			DokumentUrlInfo dokUrlInfo = getDokumentUrlInfo(docToken);
			log.info("HentDokument hentet dokumentUrlInfo for docToken={}", docToken);
			Long journalpostId = dokUrlInfo.getJournalpost().getJournalpostId();
			String filUuid = dokUrlInfo.getFilUuid();

			byte[] document = getDocument(journalpostId, filUuid, docToken);
			log.info("HentDokument hentet dokument");

			FilTypeCode filtype = getFilTypeFromJournalpost(dokUrlInfo.getJournalpost(), filUuid);

			return ResponseEntity
					.ok()
					.contentType(MediaType.parseMediaType(getContentType(filtype)))
					.contentLength(document.length)
					.body(document);
		} catch (FunctionalRecoverableException e) {
			log.error("HentDokument feilet.", e.getMessage());
			throw new DokarkivFunctionalException(e);
		}
	}

	@GetMapping(value = "ping")
	public String ping() {
		return "OK";
	}

	private DokumentUrlInfo getDokumentUrlInfo(String docToken) {
		HentDokumentUrlInfoResponse hentDokumentUrlInfoResponse = service.hentDokumentUrlInfo(new HentDokumentUrlInfoRequest(docToken));
		return hentDokumentUrlInfoResponse.getDokumentUrl();
	}

	private byte[] getDocument(final Long journalpostId, final String filUuid, final String docToken) throws NoJournalpostFoundException,
			InvalidFilUuidException, DocumentNotFoundException {
		HentDokumentRequest hentDokumentRequest = new HentDokumentRequest(journalpostId, filUuid, docToken);
		HentDokumentResponse hentDokumentResponse = service.hentDokument(hentDokumentRequest);
		return hentDokumentResponse.getDokument();
	}

	private FilTypeCode getFilTypeFromJournalpost(Journalpost journalpost, String filUuid) {
		FilDetaljer filDetaljer = journalpost.findFilDetaljerByFilUuid(filUuid);
		return filDetaljer != null ? filDetaljer.getFiltype() : null;
	}

	private String getContentType(FilTypeCode filtype) {
		return mimeTypeMapper.getMimeTypeForFileExtension(filtype.name());
	}

}