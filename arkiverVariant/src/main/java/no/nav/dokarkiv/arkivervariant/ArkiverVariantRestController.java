package no.nav.dokarkiv.arkivervariant;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.arkivervariant.rjoark102.ArkiverVariantResponse;
import no.nav.dokarkiv.arkivervariant.rjoark102.ArkiverVariantService;
import no.nav.dokarkiv.arkivervariant.rjoark102.ArkiverVariantValidator;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggHeader;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggHeaderMapper;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggHeaderException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("rest/arkivervariant")
public class ArkiverVariantRestController {

	private final no.nav.dokarkiv.arkivervariant.rjoark102.ArkiverVariantService ArkiverVariantService;
	private final AbacSecurityService abacSecurityService;
	private final AksjonsLoggService aksjonsLoggService;
	private final AksjonsLoggHeaderMapper aksjonsLoggHeaderMapper;
	private final ArkiverVariantValidator validator;


	public ArkiverVariantRestController(
			ArkiverVariantService ArkiverVariantService,
			AbacSecurityService abacSecurityService, AksjonsLoggService aksjonsLoggService, ArkiverVariantValidator validator) {
		this.ArkiverVariantService = ArkiverVariantService;
		this.abacSecurityService = abacSecurityService;
		this.aksjonsLoggService = aksjonsLoggService;
		this.validator = validator;
		this.aksjonsLoggHeaderMapper = new AksjonsLoggHeaderMapper();
	}

	@Transactional
	@ResponseBody
	@PostMapping("/{dokumentInfoId}/{variant}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark103"}, percentiles = {0.5, 0.95})
	public ArkiverVariantResponse arkiverVariant(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@PathVariable("dokumentInfoId") Long dokumentInfoId,
			@PathVariable("variant") String variant,
			@RequestBody String fil) throws UgyldigAksjonsLoggHeaderException {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark103");

		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall for arkivering av korrigert dokument med dokumentInfoId={}", dokumentInfoId);
		validator.validateArkiverVariantRequest(dokumentInfoId, variant, fil);
		abacSecurityService.assertAccessToDokumentIncludingSkjermet(dokumentInfoId);
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		List<AksjonsLoggHeader> aksjonsLoggHeader = aksjonsLoggHeaderMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString);
		aksjonsLoggService.validateAndSaveAksjon(aksjonsLoggHeader);

		ArkiverVariantResponse respons = ArkiverVariantService.arkiverVariant(dokumentInfoId, VariantFormatCode.valueOf(variant), fil);
		log.info("{} har arkivert korrigert dokument med dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), dokumentInfoId);
		return respons;
	}
}
