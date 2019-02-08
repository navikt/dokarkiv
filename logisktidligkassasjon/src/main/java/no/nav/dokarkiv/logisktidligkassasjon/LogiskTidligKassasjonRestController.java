package no.nav.dokarkiv.logisktidligkassasjon;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTOMapper;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.logisktidligkassasjon.rjoark105.LogiskTidligKassasjonResponse;
import no.nav.dokarkiv.logisktidligkassasjon.rjoark105.LogiskTidligKassasjonService;
import no.nav.dokarkiv.logisktidligkassasjon.rjoark105.LogiskTidligKassasjonValidator;
import no.nav.dokarkiv.logisktidligkassasjon.rjoark106.AngreLogiskTidligKassasjonService;
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
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/rest/logisktidligkassasjon")
public class LogiskTidligKassasjonRestController {

	private final LogiskTidligKassasjonValidator validator;
	private final LogiskTidligKassasjonService logiskTidligKassasjonService;
	private final AngreLogiskTidligKassasjonService angreLogiskTidligKassasjonService;
	private final AbacSecurityService abacSecurityService;
	private final AksjonsLoggService aksjonsLoggService;
	private final AksjonsLoggTOMapper aksjonsLoggTOMapper;

	@Inject
	public LogiskTidligKassasjonRestController(
			LogiskTidligKassasjonValidator validator,
			LogiskTidligKassasjonService logiskTidligKassasjonService,
			AngreLogiskTidligKassasjonService angreLogiskTidligKassasjonService,
			AbacSecurityService abacSecurityService,
			AksjonsLoggService aksjonsLoggService) {
		this.validator = validator;
		this.logiskTidligKassasjonService = logiskTidligKassasjonService;
		this.angreLogiskTidligKassasjonService = angreLogiskTidligKassasjonService;
		this.abacSecurityService = abacSecurityService;
		this.aksjonsLoggService = aksjonsLoggService;
		this.aksjonsLoggTOMapper = new AksjonsLoggTOMapper();
	}

	@Transactional
	@ResponseBody
	@PostMapping("/{dokumentInfoId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark105"}, percentiles = {0.5, 0.95})
	public LogiskTidligKassasjonResponse logiskTidligKassasjon(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@PathVariable("dokumentInfoId") Long dokumentInfoId) throws UgyldigAksjonsLoggException {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark105");
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall med dokumentInfoId={}", dokumentInfoId);
		validator.validerLogiskTidligKassasjonRequest(dokumentInfoId);
		abacSecurityService.assertAccessToDokumentIncludingSkjermet(dokumentInfoId);
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		LogiskTidligKassasjonResponse response = logiskTidligKassasjonService.logiskTidligKassasjonAvDokument(dokumentInfoId);

		log.info("{} har logisk tidlig kassert dokument med dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), dokumentInfoId);

		AksjonsLoggTO aksjonsLoggTO = aksjonsLoggTOMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString, AksjonsTypeCode.ENDRE_SKJERMING, null, dokumentInfoId);

		List<ArkivElementEndringTO> arkivElementEndringTOList = Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement("DokumentInfo.skjermingType")
						.fraVerdi(null)
						.tilVerdi(SkjermingTypeCode.POL.name())
						.build()

		);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTO, arkivElementEndringTOList);

		return response;
	}

	@Transactional
	@ResponseBody
	@PostMapping("/angre/{dokumentInfoId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark106"}, percentiles = {0.5, 0.95})
	public LogiskTidligKassasjonResponse angreLogiskTidligKassasjon(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@PathVariable("dokumentInfoId") Long dokumentInfoId) throws UgyldigAksjonsLoggException {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark106");
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall med dokumentInfoId={}", dokumentInfoId);
		validator.validerLogiskTidligKassasjonRequest(dokumentInfoId);
		abacSecurityService.assertAccessToDokumentIncludingSkjermet(dokumentInfoId);
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		LogiskTidligKassasjonResponse response = angreLogiskTidligKassasjonService.angreLogiskTidligKassasjonAvDokument(dokumentInfoId);
		log.info("{} har angret logisk tidlig kassering av dokument med dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), dokumentInfoId);

		AksjonsLoggTO aksjonsLoggTO = aksjonsLoggTOMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString, AksjonsTypeCode.ENDRE_SKJERMING, null, dokumentInfoId);

		List<ArkivElementEndringTO> arkivElementEndringTOList = Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement("DokumentInfo.skjermingType")
						.fraVerdi(SkjermingTypeCode.POL.name())
						.tilVerdi(null)
						.build()

		);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTO, arkivElementEndringTOList);

		return response;
	}

}
