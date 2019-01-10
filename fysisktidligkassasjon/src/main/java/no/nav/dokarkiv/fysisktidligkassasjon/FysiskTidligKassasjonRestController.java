package no.nav.dokarkiv.fysisktidligkassasjon;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.fysisktidligkassasjon.rjoark107.FysiskTidligKassasjonResponse;
import no.nav.dokarkiv.fysisktidligkassasjon.rjoark107.FysiskTidligKassasjonService;
import no.nav.dokarkiv.fysisktidligkassasjon.rjoark107.FysiskTidligKassasjonValidator;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@Slf4j
@RestController
@RequestMapping("rest/fysisktidligkassasjon")
public class FysiskTidligKassasjonRestController {

	private final FysiskTidligKassasjonValidator validator;
	private final FysiskTidligKassasjonService fysiskTidligKassasjonService;

	@Inject
	public FysiskTidligKassasjonRestController(
			FysiskTidligKassasjonValidator validator,
			FysiskTidligKassasjonService service) {
		this.validator = validator;
		this.fysiskTidligKassasjonService = service;
	}

	@Transactional
	@ResponseBody
	@PostMapping("/{dokumentInfoId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark107"}, percentiles = {0.5, 0.95})
	public FysiskTidligKassasjonResponse fysiskTidligKassasjon(@PathVariable("dokumentInfoId") Long dokumentInfoId) {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark107");
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall med dokumentInfoId={}", dokumentInfoId);
		validator.validerFysiskTidligKassasjonRequest(dokumentInfoId);
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		FysiskTidligKassasjonResponse response = fysiskTidligKassasjonService.fysiskTidligKassasjonAvDokument(dokumentInfoId);
		log.info("{} har tidlig kassert dokument med dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), dokumentInfoId);
		return response;
	}
}
