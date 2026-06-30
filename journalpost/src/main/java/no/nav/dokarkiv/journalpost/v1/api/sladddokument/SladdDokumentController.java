package no.nav.dokarkiv.journalpost.v1.api.sladddokument;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.KanIkkeSladdeDokumentException;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerOpphevSladdDokument;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerSladdDokument;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

@Tag(name = "journalpostapi - dokumenter", description = "Tjeneste for å sladde dokumenter")
@Slf4j
@Protected
@RestController
@RequestMapping("/rest/journalpostapi/v1/dokumentInfo/{dokumentInfoId}")
public class SladdDokumentController {

	private final SladdDokumentService sladdDokumentService;

	public SladdDokumentController(SladdDokumentService sladdDokumentService) {
		this.sladdDokumentService = sladdDokumentService;
	}

	@SwaggerSladdDokument
	@PostMapping(value = "/sladdDokument", consumes = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<Void> sladdDokument(
		@PathVariable long dokumentInfoId,
		@RequestBody byte[] fil) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		log.info("sladdDokument har mottatt kall om å sladde dokument med dokumentInfoId={}", dokumentInfoId);

		if (fil == null || fil.length == 0) {
			throw new KanIkkeSladdeDokumentException("sladdDokument krever en ikke-tom fil");
		}

		sladdDokumentService.sladdDokument(dokumentInfoId, fil);
		return ResponseEntity.noContent().build();
	}

	@SwaggerOpphevSladdDokument
	@PatchMapping("/opphevSladdDokument")
	public ResponseEntity<Void> opphevSladdDokument(@PathVariable long dokumentInfoId) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		log.info("opphevSladdDokument har mottatt kall om å fjerne sladding fra dokument med dokumentInfoId={}", dokumentInfoId);

		sladdDokumentService.opphevSladdDokument(dokumentInfoId);
		return ResponseEntity.noContent().build();
	}
}
