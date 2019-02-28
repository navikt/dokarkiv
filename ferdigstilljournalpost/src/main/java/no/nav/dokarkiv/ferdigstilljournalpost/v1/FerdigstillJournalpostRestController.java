package no.nav.dokarkiv.ferdigstilljournalpost.v1;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.FERDIGSTILL;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;
import static no.nav.dokarkiv.ferdigstilljournalpost.v1.util.Utils.validateId;
import static no.nav.dokarkiv.ferdigstilljournalpost.v1.util.Utils.validateJournalfEnhet;
import static org.apache.commons.lang3.StringUtils.isBlank;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTOMapper;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.ferdigstilljournalpost.v1.api.FerdigstillJournalpostRequest;
import no.nav.dokarkiv.ferdigstilljournalpost.v1.swagger.SwaggerFerdigstillJournalpost;
import no.nav.dokarkiv.ferdigstilljournalpost.v1.rjoark201.FerdigstillJournalpostService;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/rest/v1/journalpost")
@Api
public class FerdigstillJournalpostRestController {

	private final FerdigstillJournalpostService ferdigstillJournalpostService;
	private final AbacSecurityService abacSecurityService;
	private final AksjonsLoggService aksjonsLoggService;
	private final AksjonsLoggTOMapper aksjonsLoggTOMapper;

	@Inject
	public FerdigstillJournalpostRestController(final FerdigstillJournalpostService ferdigstillJournalpostService,
												final AbacSecurityService abacSecurityService,
												final AksjonsLoggService aksjonsLoggService) {
		this.ferdigstillJournalpostService = ferdigstillJournalpostService;
		this.abacSecurityService = abacSecurityService;
		this.aksjonsLoggService = aksjonsLoggService;
		this.aksjonsLoggTOMapper = new AksjonsLoggTOMapper();
	}

	@Transactional
	@SwaggerFerdigstillJournalpost
	@PatchMapping("/{journalpostId}/ferdigstill")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark201"}, percentiles = {0.5, 0.95})
	public ResponseEntity<String> ferdigstillJournalpost(
			@RequestHeader(value = AKSJONS_LOGG_HEADER, required = false) String aksjonsLoggHeaderString,
			@PathVariable @ApiParam(value = "IDen til journalposten som skal ferdigstilles", required = true, example = "77778888") String journalpostId,
			@RequestBody FerdigstillJournalpostRequest request) throws UgyldigAksjonsLoggException {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark201");
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall for ferdigstilling av journalpost med journalpostId={}", journalpostId);
		validateRequest(journalpostId, request);
		abacSecurityService.assertAccessToJournalpost(journalpostId);
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		List<ArkivElementEndringTO> arkivElementEndringTOList = ferdigstillJournalpostService.ferdigstill(journalpostId, request.getJournalfEnhet());

		populerAksjonslogg(journalpostId, aksjonsLoggHeaderString, arkivElementEndringTOList);

		return ResponseEntity.ok().body("Journalpost ferdigstilt");
	}

	private void validateRequest(String journalpostId, FerdigstillJournalpostRequest request) {
		validateId(journalpostId, "journalpostId");
		validateJournalfEnhet(request.getJournalfEnhet(), "journalfEnhet");
	}

	private void populerAksjonslogg(String journalpostId, String aksjonsLoggHeaderString, List<ArkivElementEndringTO> arkivElementEndringTOList) throws UgyldigAksjonsLoggException {
		AksjonsLoggTO aksjonsLoggTo;
		if (isBlank(aksjonsLoggHeaderString)) {
			aksjonsLoggTo = AksjonsLoggTO.builder()
					.aksjon(FERDIGSTILL)
					.journalpostId(Long.parseLong(journalpostId))
					.utfoertAv(MDC.get(MDC_USER_ID))
					.bruker(MDC.get(MDC_USER_ID))
					.melding(String.format("Journalpost ferdigstilt, journalpostId=%s", journalpostId))
					.build();
		} else {
			aksjonsLoggTo = aksjonsLoggTOMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString, FERDIGSTILL, Long.parseLong(journalpostId), null);
		}

		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTo, arkivElementEndringTOList);
	}
}
