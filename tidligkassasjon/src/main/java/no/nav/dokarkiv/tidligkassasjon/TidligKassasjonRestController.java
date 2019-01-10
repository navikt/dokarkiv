package no.nav.dokarkiv.tidligkassasjon;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_INFO_HEADER;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.exceptions.UgyldigHendelseLoggInfoException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.tidligkassasjon.rjoark107.TidligKassasjonResponse;
import no.nav.dokarkiv.tidligkassasjon.rjoark107.TidligKassasjonService;
import no.nav.dokarkiv.tidligkassasjon.rjoark107.TidligKassasjonValidator;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@Slf4j
@RestController
@RequestMapping("rest/tidligkassasjon")
public class TidligKassasjonRestController {

	private final TidligKassasjonValidator validator;
	private final TidligKassasjonService tidligKassasjonService;
	private final AksjonsLoggService aksjonsLoggService;
	@Inject
	public TidligKassasjonRestController(
			TidligKassasjonValidator validator,
			TidligKassasjonService service,
			AbacSecurityService abacSecurityService, AksjonsLoggService aksjonsLoggService) {
		this.validator = validator;
		this.tidligKassasjonService = service;
		this.aksjonsLoggService = aksjonsLoggService;
	}

	@Transactional
	@ResponseBody
	@PostMapping("/{dokumentInfoId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark107"}, percentiles = {0.5, 0.95})
	public TidligKassasjonResponse tidligKassasjon(@RequestHeader(value = AKSJONS_INFO_HEADER) String aksjonsInfoHeader,
												   @PathVariable("dokumentInfoId") Long dokumentInfoId) throws UgyldigHendelseLoggInfoException {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark107");
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall med dokumentInfoId={}", dokumentInfoId);
		validator.validerTidligKassasjonRequest(dokumentInfoId);
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		aksjonsLoggService.validerOgLagreAksjon(aksjonsInfoHeader);
		TidligKassasjonResponse response = tidligKassasjonService.tidligKassasjonAvDokument(dokumentInfoId);
		log.info("{} har tidlig kassert dokument med dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), dokumentInfoId);
		return response;
	}
}
