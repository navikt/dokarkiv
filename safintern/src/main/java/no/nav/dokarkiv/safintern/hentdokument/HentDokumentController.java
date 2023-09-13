package no.nav.dokarkiv.safintern.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.util.MimeTypeMapper;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.safintern.SafinternConstants.BASE_PATH;
import static no.nav.dokarkiv.safintern.SafinternConstants.ROLE_CLAIM_TILGANG;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@Slf4j
@RestController
@RequestMapping(BASE_PATH)
@ProtectedWithClaims(issuer = ISSUER_AZUREV2, claimMap = {"roles=" + ROLE_CLAIM_TILGANG})
class HentDokumentController {

	private final HentDokumentService hentDokumentService;
	private final MimeTypeMapper mimeTypeMapper;

	public HentDokumentController(HentDokumentService hentDokumentService) {
		this.hentDokumentService = hentDokumentService;
		this.mimeTypeMapper = new MimeTypeMapper();
	}

	@GetMapping(value = "/hentdokument/{dokumentInfoId}/{variant}")
	public ResponseEntity<InputStreamResource> hentDokument(@PathVariable Long dokumentInfoId,
															@PathVariable VariantFormatCode variant) {
		log.info("safintern/hentDokument henter dokument med dokumentInfoId={}, variant={}", dokumentInfoId, variant);
		HentDokumentResponse hentDokumentResponse = hentDokumentService.hentDokumentBy(dokumentInfoId, variant);
		String mimeTypeForFileExtension = mimeTypeMapper.getMimeTypeForFileExtension(hentDokumentResponse.filtype());
		log.info("safintern/hentDokument hentet dokument med dokumentInfoId={}, variant={}, Content-Type={}, Content-Length={}",
				dokumentInfoId, variant, mimeTypeForFileExtension, hentDokumentResponse.dokumentLength());

		return ResponseEntity.ok()
				.header(CONTENT_TYPE, mimeTypeForFileExtension)
				.contentLength(hentDokumentResponse.dokumentLength())
				.body(new InputStreamResource(hentDokumentResponse.dokument()));
	}
}
