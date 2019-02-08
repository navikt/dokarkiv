package no.nav.dokarkiv.arkiverkorrigertdokument;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103.ArkiverKorrigertDokumentRespons;
import no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103.ArkiverKorrigertDokumentService;
import no.nav.dokarkiv.arkiverkorrigertdokument.rjoark104.AngreArkiverKorrigertDokumentService;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTOMapper;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
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

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("rest/arkiverkorrigertdokument")
public class ArkiverKorrigertDokumentRestController {

	private final ArkiverKorrigertDokumentService arkiverKorrigertDokumentService;
	private final AngreArkiverKorrigertDokumentService angreArkiverKorrigertDokumentService;
	private final AbacSecurityService abacSecurityService;
	private final AksjonsLoggService aksjonsLoggService;
	private final AksjonsLoggTOMapper aksjonsLoggTOMapper;

	public ArkiverKorrigertDokumentRestController(
			ArkiverKorrigertDokumentService arkiverKorrigertDokumentService,
			AngreArkiverKorrigertDokumentService angreArkiverKorrigertDokumentService,
			AbacSecurityService abacSecurityService, AksjonsLoggService aksjonsLoggService) {
		this.arkiverKorrigertDokumentService = arkiverKorrigertDokumentService;
		this.angreArkiverKorrigertDokumentService = angreArkiverKorrigertDokumentService;
		this.abacSecurityService = abacSecurityService;
		this.aksjonsLoggService = aksjonsLoggService;
		this.aksjonsLoggTOMapper = new AksjonsLoggTOMapper();
	}

	@Transactional
	@ResponseBody
	@PostMapping("/{dokumentInfoId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark103"}, percentiles = {0.5, 0.95})
	public ArkiverKorrigertDokumentRespons arkiverKorrigertDokument(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@PathVariable("dokumentInfoId") Long dokumentInfoId,
			@RequestBody String fil) throws UgyldigAksjonsLoggException {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark103");

		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall for arkivering av korrigert dokument med dokumentInfoId={}", dokumentInfoId);
		abacSecurityService.assertAccessToDokumentIncludingSkjermet(dokumentInfoId);
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		FilDetaljer filDetaljer = arkiverKorrigertDokumentService.arkiverKorrigertDokument(dokumentInfoId, fil);

		AksjonsLoggTO aksjonsLoggTO = aksjonsLoggTOMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString, AksjonsTypeCode.ARKIVERING, null, dokumentInfoId);
		List<ArkivElementEndringTO> arkivElementEndringTOList = Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement("Fildetaljer.filUuid")
						.fraVerdi(null)
						.tilVerdi(filDetaljer.getFilUuid())
						.build(),
				ArkivElementEndringTO.builder()
						.arkivElement("Fildetaljer.variantFormat")
						.fraVerdi(null)
						.tilVerdi(VariantFormatCode.SLADDET.name())
						.build(),
				ArkivElementEndringTO.builder()
						.arkivElement("FilDetaljer.variantFormat[ARKIV].skjermingType")
						.fraVerdi(null)
						.tilVerdi(SkjermingTypeCode.POL.name())
						.build()
		);

		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTO, arkivElementEndringTOList);

		log.info("{} har arkivert korrigert dokument med dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), dokumentInfoId);
		return ArkiverKorrigertDokumentRespons.builder()
				.dokumentInfoId(dokumentInfoId)
				.build();
	}


	@Transactional
	@ResponseBody
	@PostMapping("/angre/{dokumentInfoId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark103"}, percentiles = {0.5, 0.95})
	public ArkiverKorrigertDokumentRespons angreArkiverKorrigertDokument(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@PathVariable("dokumentInfoId") Long dokumentInfoId) throws UgyldigAksjonsLoggException {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark104");
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall for angre korrigering av dokument med dokumentInfoId={}", dokumentInfoId);
		abacSecurityService.assertAccessToDokumentIncludingSkjermet(dokumentInfoId);
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		ArkiverKorrigertDokumentRespons respons = angreArkiverKorrigertDokumentService.angreArkiverKorrigertDokument(dokumentInfoId);

		AksjonsLoggTO aksjonsLoggTO = aksjonsLoggTOMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString, AksjonsTypeCode.SLETT, null, dokumentInfoId);
		List<ArkivElementEndringTO> arkivElementEndringTOList = Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement("FilDetaljer.variantFormat")
						.fraVerdi(VariantFormatCode.SLADDET.name())
						.tilVerdi(null)
						.build(),
				ArkivElementEndringTO.builder()
						.arkivElement("FilDetaljer.variantFormat[ARKIV].skjermingType")
						.fraVerdi(SkjermingTypeCode.POL.name())
						.tilVerdi(null)
						.build()
		);

		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTO, arkivElementEndringTOList);
		log.info("{} har angret arkivering av korrigert dokument med dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), dokumentInfoId);
		return respons;
	}

}
