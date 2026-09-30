package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.dokumentfil.DokumentFilResponse;
import no.nav.dokarkiv.journalpost.v1.services.LastOppDokumentFilResult;
import no.nav.dokarkiv.journalpost.v1.services.LastOppDokumentFilService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerLastOppDokumentFil;
import no.nav.dokarkiv.journalpost.v1.validators.LastOppDokumentFilValidator;
import no.nav.dokarkiv.journalpost.v1.validators.Sha256ContentDigest;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.journalpost.v1.swagger.SwaggerLastOppDokumentFil.DOCS_HEADER_CONTENT_DIGEST;
import static no.nav.dokarkiv.journalpost.v1.swagger.SwaggerLastOppDokumentFil.DOCS_HEADER_CONTENT_TYPE;
import static no.nav.dokarkiv.journalpost.v1.swagger.SwaggerLastOppDokumentFil.DOCS_HEADER_IDEMPOTENCY_KEY;
import static no.nav.dokarkiv.journalpost.v1.swagger.SwaggerLastOppDokumentFil.TILLATTE_MEDIA_TYPER;
import static no.nav.dokarkiv.journalpost.v1.validators.LastOppDokumentFilValidator.validate;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@Protected
@RestController
@Tag(name = "journalpostapi - dokumentFil", description = "Tjenester for DokumentFil")
@RequestMapping("/rest/journalpostapi/v1/dokumentFil")
public class DokumentFilController {

	private final LastOppDokumentFilService lastOppDokumentFilService;

	public DokumentFilController(LastOppDokumentFilService lastOppDokumentFilService) {
		this.lastOppDokumentFilService = lastOppDokumentFilService;
	}

	@SwaggerLastOppDokumentFil
	@PostMapping
	public ResponseEntity<DokumentFilResponse> lastOppDokumentFil(
			@Parameter(description = DOCS_HEADER_CONTENT_TYPE, example = "application/pdf", required = true)
			@RequestHeader(value = CONTENT_TYPE) String contentType,
			@Parameter(description = DOCS_HEADER_CONTENT_DIGEST, example = "sha-256=:Ksa5C3tEwR/Yenu/e3L6UYKX04SVyYsu8Ib3h7qNOSo=:", required = true)
			@RequestHeader(value = "Content-Digest") String contentDigestHeader,
			@Parameter(description = DOCS_HEADER_IDEMPOTENCY_KEY, example = "41ff35f0-dae9-41f8-80b0-4137592c16ca", required = true, schema = @Schema(maxLength = 512))
			@RequestHeader(value = "Idempotency-Key") String idempotencyKey,
			InputStream requestBody) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		Sha256ContentDigest sha256ContentDigest = validate(contentType, contentDigestHeader, idempotencyKey);

		log.info("lastOppDokumentFil har mottatt kall for å laste opp en dokumentFil med eksternDokumentReferanseId={}, sha256={}",
				idempotencyKey, sha256ContentDigest.base64());

		LastOppDokumentFilResult lastOppDokumentFilResult = lastOppDokumentFilService.lastOppDokumentFil(idempotencyKey, sha256ContentDigest, contentType, requestBody);

		switch (lastOppDokumentFilResult.utfall()) {
			case TEKNISK_RETRY ->
					log.info("lastOppDokumentFil mottok et nettverk-retry for allerede fullført opplasting med eksternDokumentReferanseId={}, dokumentFilId={}",
							lastOppDokumentFilResult.eksternDokumentReferanseId(), lastOppDokumentFilResult.dokumentFilId());
			case LASTET_OPP ->
					log.info("lastOppDokumentFil har lastet opp eksternDokumentReferanseId={}, dokumentFilId={}",
							lastOppDokumentFilResult.eksternDokumentReferanseId(), lastOppDokumentFilResult.dokumentFilId());
		}

		return ResponseEntity.status(CREATED)
				.body(DokumentFilResponse.fra(lastOppDokumentFilResult));
	}
}
