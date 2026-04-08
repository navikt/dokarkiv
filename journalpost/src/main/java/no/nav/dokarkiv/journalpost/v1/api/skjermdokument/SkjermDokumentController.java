package no.nav.dokarkiv.journalpost.v1.api.skjermdokument;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerAngreSkjermDokument;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerSkjermDokument;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

@Tag(name = "journalpostapi - dokumenter", description = "Tjeneste for å skjerme dokumenter")
@Slf4j
@Protected
@RestController
@RequestMapping("/rest/journalpostapi/v1/dokumentInfo/{dokumentInfoId}/skjermDokument")
public class SkjermDokumentController {

	private final SkjermDokumentService skjermDokumentService;

	public SkjermDokumentController(SkjermDokumentService skjermDokumentService) {
		this.skjermDokumentService = skjermDokumentService;
	}

	@SwaggerSkjermDokument
	@PatchMapping
	public ResponseEntity<String> skjermDokument(
		@PathVariable long dokumentInfoId,
		@RequestBody @Valid SkjermDokumentRequest request) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		log.info("skjermdokument har mottatt kall om å skjerme dokument med dokumentInfoId={} med hjemmel={}",
			dokumentInfoId, request.hjemmel());

		skjermDokumentService.skjermDokumentMedDokumentInfoId(dokumentInfoId, request.hjemmel());
		return ResponseEntity.noContent().build();
	}

	@SwaggerAngreSkjermDokument
	@PatchMapping("/angre")
	public ResponseEntity<String> angreSkjermDokument(@PathVariable long dokumentInfoId) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		log.info("angreSkjermDokument har mottatt kall om å fjerne skjerming fra dokument med dokumentInfoId={}", dokumentInfoId);

		skjermDokumentService.angreSkjermDokumentMedDokumentInfoId(dokumentInfoId);
		return ResponseEntity.noContent().build();
	}
}
