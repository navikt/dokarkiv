package no.nav.dokarkiv.kasserdokument;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggHeader;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggHeaderMapper;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggHeaderException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.kasserdokument.rjoark103.KasserDokumentRequest;
import no.nav.dokarkiv.kasserdokument.rjoark103.KasserDokumentResponse;
import no.nav.dokarkiv.kasserdokument.rjoark103.KasserDokumentService;
import no.nav.dokarkiv.kasserdokument.rjoark103.KasserDokumentValidator;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("rest/")
public class KasserDokumentRestController {

	private final KasserDokumentValidator validator;
	private final AksjonsLoggService aksjonsLoggService;
	private final AksjonsLoggHeaderMapper aksjonsLoggHeaderMapper;
	private final KasserDokumentService kasserDokumentService;

	@Inject
	public KasserDokumentRestController(
			KasserDokumentValidator validator,
			KasserDokumentService service,
			AksjonsLoggService aksjonsLoggService) {
		this.validator = validator;
		this.kasserDokumentService = service;
		this.aksjonsLoggService = aksjonsLoggService;
		this.aksjonsLoggHeaderMapper = new AksjonsLoggHeaderMapper();
	}

	@Transactional
	@ResponseBody
	@DeleteMapping("kasserdokument")
	//TODO: MÅ endre fra value = UPDATE_ACTION til DELETE_ACTION. Men joarkadmin har ikke tilgang.
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark103"}, percentiles = {0.5, 0.95})
	public KasserDokumentResponse kasserDokument(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@RequestBody KasserDokumentRequest request) throws UgyldigAksjonsLoggHeaderException {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark107");
		validator.validerKasserDokumentRequest(request);
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall med dokumentInfoId={}", request.getDokumentInfoId());
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		List<AksjonsLoggHeader> aksjonsLoggHeader = aksjonsLoggHeaderMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString);
		aksjonsLoggService.validateAndSaveAksjon(aksjonsLoggHeader);
		KasserDokumentResponse response = kasserDokumentService.kasserDokument(request);
		log.info("{} har fysisk tidlig kassert dokument med dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), request.getDokumentInfoId());
		return response;
	}
}
