package no.nav.dokarkiv.arkivervariant;

import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HJEMMEL_HEADER;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_MELDING_HEADER;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_UTFOERT_AV_HEADER;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.arkivervariant.rjoark103.ArkiverVariantRequest;
import no.nav.dokarkiv.arkivervariant.rjoark103.ArkiverVariantResponse;
import no.nav.dokarkiv.arkivervariant.rjoark103.ArkiverVariantService;
import no.nav.dokarkiv.arkivervariant.rjoark103.ArkiverVariantValidator;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("rest/admin")
public class ArkiverVariantRestController {

	private final ArkiverVariantService arkiverVariantService;
	private final ArkiverVariantValidator validator;


	public ArkiverVariantRestController(
			ArkiverVariantService arkiverVariantService,
			ArkiverVariantValidator validator) {
		this.arkiverVariantService = arkiverVariantService;
		this.validator = validator;
	}

	@Transactional
	@ResponseBody
	@PostMapping("/arkivervariant")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark103"}, percentiles = {0.5, 0.95})
	public ArkiverVariantResponse arkiverVariant(
			@RequestHeader(value = AKSJONS_LOGG_HJEMMEL_HEADER) String hjemmel,
			@RequestHeader(value = AKSJONS_LOGG_MELDING_HEADER) String melding,
			@RequestHeader(value = AKSJONS_LOGG_UTFOERT_AV_HEADER, required = false) String utfoertAv,
			@RequestBody ArkiverVariantRequest request) throws UgyldigAksjonsLoggException {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark103");
		validator.validateArkiverVariantRequest(request);
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall for arkivering av korrigert dokument med dokumentInfoId={}", request
				.getDokumentInfoId());
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		ArkiverVariantResponse respons = arkiverVariantService.arkiverVariant(request, melding, utfoertAv, hjemmel);
		log.info("{} har arkivert variant= {} med dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), request.getVariant(), request.getDokumentInfoId());
		return respons;
	}
}
