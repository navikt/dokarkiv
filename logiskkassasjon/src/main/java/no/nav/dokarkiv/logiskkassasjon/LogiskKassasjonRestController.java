package no.nav.dokarkiv.logiskkassasjon;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_INFO_HEADER;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggInfoException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.logiskkassasjon.rjoark105.LogiskKassasjonResponse;
import no.nav.dokarkiv.logiskkassasjon.rjoark105.LogiskKassasjonService;
import no.nav.dokarkiv.logiskkassasjon.rjoark105.LogiskKassasjonValidator;
import no.nav.dokarkiv.logiskkassasjon.rjoark106.AngreLogiskKassasjonService;
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
@RequestMapping("/rest/logiskkassasjon")
public class LogiskKassasjonRestController {

	private final LogiskKassasjonValidator validator;
	private final LogiskKassasjonService logiskKassasjonService;
	private final AngreLogiskKassasjonService angreLogiskKassasjonService;
	private final AbacSecurityService abacSecurityService;
	private final AksjonsLoggService aksjonsLoggService;
	@Inject
	public LogiskKassasjonRestController(
			LogiskKassasjonValidator validator,
			LogiskKassasjonService logiskKassasjonService,
			AngreLogiskKassasjonService angreLogiskKassasjonService,
			AbacSecurityService abacSecurityService, AksjonsLoggService aksjonsLoggService) {
		this.validator = validator;
		this.logiskKassasjonService = logiskKassasjonService;
		this.angreLogiskKassasjonService = angreLogiskKassasjonService;
		this.abacSecurityService = abacSecurityService;
		this.aksjonsLoggService = aksjonsLoggService;
	}

	@Transactional
	@ResponseBody
	@PostMapping("/{dokumentInfoId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark105"}, percentiles = {0.5, 0.95})
	public LogiskKassasjonResponse logiskKassasjon(@RequestHeader(value = AKSJONS_INFO_HEADER) String hendelseInfoHeader,
												   @PathVariable("dokumentInfoId") Long dokumentInfoId) throws UgyldigAksjonsLoggInfoException {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark105");
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall med dokumentInfoId={}", dokumentInfoId);
		validator.validerLogiskKassasjonRequest(dokumentInfoId);
		abacSecurityService.assertAccessToDokumentIncludingBegrenset(dokumentInfoId);
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		aksjonsLoggService.validerOgLagreAksjon(hendelseInfoHeader);
		LogiskKassasjonResponse response = logiskKassasjonService.logiskKassasjonAvDokument(dokumentInfoId);
		log.info("{} har logisk kassert dokument med dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), dokumentInfoId);
		return response;
	}

	@Transactional
	@ResponseBody
	@PostMapping("/angre/{dokumentInfoId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark106"}, percentiles = {0.5, 0.95})
	public LogiskKassasjonResponse angreLogiskKassasjon(@RequestHeader(value = AKSJONS_INFO_HEADER) String hendelseInfoHeader,
														@PathVariable("dokumentInfoId") Long dokumentInfoId) throws UgyldigAksjonsLoggInfoException {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark106");
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall med dokumentInfoId={}", dokumentInfoId);
		validator.validerLogiskKassasjonRequest(dokumentInfoId);
		abacSecurityService.assertAccessToDokumentIncludingBegrenset(dokumentInfoId);
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		aksjonsLoggService.validerOgLagreAksjon(hendelseInfoHeader);
		LogiskKassasjonResponse response = angreLogiskKassasjonService.angreLogiskKassasjonAvDokument(dokumentInfoId);
		log.info("{} har angret logisk kassering av dokument med dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), dokumentInfoId);
		return response;
	}

}
