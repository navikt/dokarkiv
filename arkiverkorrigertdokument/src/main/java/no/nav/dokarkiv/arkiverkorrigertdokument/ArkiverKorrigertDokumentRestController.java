package no.nav.dokarkiv.arkiverkorrigertdokument;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103.ArkiverKorrigertDokumentRequest;
import no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103.ArkiverKorrigertDokumentRespons;
import no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103.ArkiverKorrigertDokumentService;
import no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103.ArkiverKorrigertDokumentValidator;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("rest/arkiverkorrigertdokument")
public class ArkiverKorrigertDokumentRestController {

	private final ArkiverKorrigertDokumentService arkiverKorrigertDokumentService;
	private final AbacSecurityService abacSecurityService;
	private final ArkiverKorrigertDokumentValidator validator;

	public ArkiverKorrigertDokumentRestController(
			ArkiverKorrigertDokumentService arkiverKorrigertDokumentService,
			AbacSecurityService abacSecurityService, ArkiverKorrigertDokumentValidator validator) {
		this.arkiverKorrigertDokumentService = arkiverKorrigertDokumentService;
		this.abacSecurityService = abacSecurityService;
		this.validator = validator;
	}

	@Transactional
	@ResponseBody
	@PostMapping("/")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark103"}, percentiles = {0.5, 0.95})
	public ArkiverKorrigertDokumentRespons arkiverKorrigertDokument(@RequestBody ArkiverKorrigertDokumentRequest request) {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark103");
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall med dokumentInfoId={}", request.getDokumentInfoId());
		validator.validateArkiverKorrigertDokumentRequest(request);
		abacSecurityService.assertAccessToDokumentIncludingBegrenset(request.getDokumentInfoId());
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		ArkiverKorrigertDokumentRespons respons = arkiverKorrigertDokumentService.arkiverKorrigertDokument(request);
		log.info("{} har arkivert korrigert dokument med dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), request.getDokumentInfoId());
		return respons;
	}

}
