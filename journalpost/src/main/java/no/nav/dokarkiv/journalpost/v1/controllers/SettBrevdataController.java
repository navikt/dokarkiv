package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.journalpost.v1.api.settbrevdata.SettBrevdataResponse;
import no.nav.dokarkiv.journalpost.v1.services.SettBrevdata;
import no.nav.dokarkiv.journalpost.v1.services.SettBrevdataService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerSettBrevdata;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.valueOf;
import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.core.stelvio.RequestContextUtil.createAndSetUsername;
import static no.nav.dokarkiv.journalpost.v1.controllers.SettBrevdataController.INTERN_ROLE_BREVSERVER;
import static no.nav.dokarkiv.journalpost.v1.validators.SettBrevdataValidator.validate;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.CREATED;

@Tag(name = "journalpostapi - internt", description = "Intern tjeneste for brevserver")
@Slf4j
@RestController
@RequestMapping("/rest/internal/journalpostapi/v1")
@ProtectedWithClaims(issuer = ISSUER_AZUREV2, claimMap = {"roles=" + INTERN_ROLE_BREVSERVER})
public class SettBrevdataController {
	public static final String INTERN_ROLE_BREVSERVER = "api_intern_brevserver";
	public static final String VARIANT_FORMAT_ARKIV = "ARKIV";
	public static final String VARIANT_FORMAT_PRODUKSJON = "PRODUKSJON";

	private final SettBrevdataService settBrevdataService;

	public SettBrevdataController(SettBrevdataService settBrevdataService) {
		this.settBrevdataService = settBrevdataService;
	}

	@SwaggerSettBrevdata
	@PostMapping("/journalpost/{journalpostId}/settBrevdata/{variantFormat}")
	public ResponseEntity<SettBrevdataResponse> settBrevdata(@RequestHeader(value = CONTENT_TYPE) String contentType,
															 @PathVariable long journalpostId,
															 @PathVariable String variantFormat,
															 @RequestBody byte[] brevdata) {
		createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		validate(contentType, variantFormat, brevdata);
		log.info("settBrevdata har mottatt kall for å sette brevdata på journalpostId={}, variantFormat={}", journalpostId, variantFormat);

		SettBrevdata settBrevdata = settBrevdataService.settBrevdata(journalpostId, valueOf(variantFormat), brevdata);
		SettBrevdataResponse settBrevdataResponse = SettBrevdataResponse.from(settBrevdata);
		return loggMapResponse(journalpostId, variantFormat, settBrevdata)
				.body(settBrevdataResponse);
	}

	private ResponseEntity.BodyBuilder loggMapResponse(long journalpostId, String variantFormat, SettBrevdata settBrevdata) {
		return switch (settBrevdata.handling()) {
			case OPPRETTET_DOKUMENT -> {
				log.info("settBrevdata har opprettet DokumentFil med brevdata. journalpostId={}, variantFormat={}, filUuid={}",
						journalpostId, variantFormat, settBrevdata.filUuid());
				yield ResponseEntity.status(CREATED);
			}
			case OPPDATERT_DOKUMENT -> {
				log.info("settBrevdata har oppdatert brevdata på DokumentFil. journalpostId={}, variantFormat={}, filUuid={}",
						journalpostId, variantFormat, settBrevdata.filUuid());
				yield ResponseEntity.ok();
			}
			case INGEN -> {
				log.info("settBrevdata oppdaterer ikke brevdata. DokumentFil finnes allerede. journalpostId={}, variantFormat={}, filUuid={}",
						journalpostId, variantFormat, settBrevdata.filUuid());
				yield ResponseEntity.ok();
			}
		};
	}
}
