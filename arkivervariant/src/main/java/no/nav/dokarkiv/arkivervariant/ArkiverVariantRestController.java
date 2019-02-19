package no.nav.dokarkiv.arkivervariant;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.arkivervariant.rjoark102.ArkiverVariantRequest;
import no.nav.dokarkiv.arkivervariant.rjoark102.ArkiverVariantResponse;
import no.nav.dokarkiv.arkivervariant.rjoark102.ArkiverVariantService;
import no.nav.dokarkiv.arkivervariant.rjoark102.ArkiverVariantValidator;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTOMapper;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("rest/")
public class ArkiverVariantRestController {

	private final ArkiverVariantService arkiverVariantService;
	private final AbacSecurityService abacSecurityService;
	private final AksjonsLoggService aksjonsLoggService;
	private final AksjonsLoggTOMapper aksjonsLoggTOMapper;
	private final ArkiverVariantValidator validator;


	public ArkiverVariantRestController(
			ArkiverVariantService arkiverVariantService,
			AbacSecurityService abacSecurityService, AksjonsLoggService aksjonsLoggService, ArkiverVariantValidator validator) {
		this.arkiverVariantService = arkiverVariantService;
		this.abacSecurityService = abacSecurityService;
		this.aksjonsLoggService = aksjonsLoggService;
		this.validator = validator;
		this.aksjonsLoggTOMapper = new AksjonsLoggTOMapper();
	}

	@Transactional
	@ResponseBody
	@PostMapping("arkivervariant")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark103"}, percentiles = {0.5, 0.95})
	public ArkiverVariantResponse arkiverVariant(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@RequestBody ArkiverVariantRequest request) throws UgyldigAksjonsLoggException {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark103");
		validator.validateArkiverVariantRequest(request);
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall for arkivering av korrigert dokument med dokumentInfoId={}", request.getDokumentInfoId());
		abacSecurityService.assertAccessToDokumentIncludingSkjermet(request.getDokumentInfoId());
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		ArkiverVariantResponse respons = arkiverVariantService.arkiverVariant(request);

		List<ArkivElementEndringTO> arkivElementEndringTOList = Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement("Fildetaljer.filUuid")
						.fraVerdi(null)
						.tilVerdi(respons.getFilUuid())
						.build(),
				ArkivElementEndringTO.builder()
						.arkivElement("Fildetaljer.variantFormat")
						.fraVerdi(null)
						.tilVerdi(request.getVariant().name())
						.build()
		);

		AksjonsLoggTO aksjonsLoggTO = aksjonsLoggTOMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString, AksjonsTypeCode.ARKIVERING, null, request
				.getDokumentInfoId());
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTO, arkivElementEndringTOList);

		log.info("{} har arkivert variant= {} med dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), request.getVariant(), request.getDokumentInfoId());
		return respons;
	}
}
