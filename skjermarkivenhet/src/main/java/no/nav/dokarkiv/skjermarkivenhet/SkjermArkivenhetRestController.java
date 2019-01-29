package no.nav.dokarkiv.skjermarkivenhet;

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
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggHeaderException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.skjermarkivenhet.rjoark100.SkjermArkivenhetRequestTo;
import no.nav.dokarkiv.skjermarkivenhet.rjoark100.SkjermArkivenhetResponse;
import no.nav.dokarkiv.skjermarkivenhet.rjoark100.SkjermArkivenhetService;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("rest")
public class SkjermArkivenhetRestController {

	private final static String REQUEST_BODY = "body"

	private final AbacSecurityService abacSecurityService;
	private final SkjermArkivenhetService skjermArkivenhetService;
	private final AksjonsLoggService aksjonsLoggService;
	private final AksjonsLoggHeaderMapper aksjonsLoggHeaderMapper;


	@Inject
	public SkjermArkivenhetRestController(
			AbacSecurityService abacSecurityService,
			SkjermArkivenhetService skjermArkivenhetService,
			AksjonsLoggService aksjonsLoggService,
			AksjonsLoggHeaderMapper aksjonsLoggHeaderMapper) {
		this.abacSecurityService = abacSecurityService;
		this.skjermArkivenhetService = skjermArkivenhetService;
		this.aksjonsLoggService = aksjonsLoggService;
		this.aksjonsLoggHeaderMapper = aksjonsLoggHeaderMapper;
	}

	@Transactional
	@ResponseBody
	@PostMapping("/skjermarkivenhet")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark100"}, percentiles = {0.5, 0.95})
	public SkjermArkivenhetResponse skjermJournalpost(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@RequestBody String jsonStringBody,
			@PathVariable("skjermingType") SkjermingTypeCode skjermingType,
			@PathVariable("journalpostId") Long journalpostId) throws UgyldigAksjonsLoggHeaderException {
		sjekkAbacOgOpprettRequestContext(journalpostId.toString());
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall med journalpostId=" + journalpostId);
		validerOgLagreListeMedAksjonsLoggHeader(aksjonsLoggHeaderString);
		SkjermArkivenhetResponse response = skjermArkivenhetService.skjermArkivenhet(
				SkjermArkivenhetRequestTo.builder()
						.arkivenhet(ArkivenhetCode.JOURNALPOST)
						.skjermingType(skjermingType)
						.journalpostId(journalpostId)
						.build());
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har utført slett av journalpost med journalpostId={}", journalpostId);
		return response;
	}


	/**
	 * skjermJournalpost - tidligere logiskSlettDokument(HOVEDDOKUMENT)
	 * <p>
	 * POST,
	 * /rest/skjermArkivenhet/pol/journalpost/{journalpostId},
	 * header(aksjonslogg){
	 * skjermArkivenhetService.skjermArkivenhet(pol, journalpost, journalpostId, null, null);
	 * }
	 */
	@Transactional
	@ResponseBody
	@PostMapping("/journalpost/{skjermingType}/{journalpostId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark100"}, percentiles = {0.5, 0.95})
	public SkjermArkivenhetResponse skjermJournalpost(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@PathVariable("skjermingType") SkjermingTypeCode skjermingType,
			@PathVariable("journalpostId") Long journalpostId) throws UgyldigAksjonsLoggHeaderException {
		sjekkAbacOgOpprettRequestContext(journalpostId.toString());
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall med journalpostId=" + journalpostId);
		validerOgLagreListeMedAksjonsLoggHeader(aksjonsLoggHeaderString);
		SkjermArkivenhetResponse response = skjermArkivenhetService.skjermArkivenhet(
				SkjermArkivenhetRequestTo.builder()
						.arkivenhet(ArkivenhetCode.JOURNALPOST)
						.skjermingType(skjermingType)
						.journalpostId(journalpostId)
						.build());
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har utført slett av journalpost med journalpostId={}", journalpostId);
		return response;
	}

	@Transactional
	@ResponseBody
	@DeleteMapping("/journalpost/{skjermingType}/{journalpostId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark100"}, percentiles = {0.5, 0.95})
	public SkjermArkivenhetResponse opphevSkjermJournalpost(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@PathVariable("skjermingType") SkjermingTypeCode skjermingType,
			@PathVariable("journalpostId") Long journalpostId) throws UgyldigAksjonsLoggHeaderException {
		sjekkAbacOgOpprettRequestContext(journalpostId.toString());
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall med journalpostId=" + journalpostId);
		validerOgLagreListeMedAksjonsLoggHeader(aksjonsLoggHeaderString);
		SkjermArkivenhetResponse response = skjermArkivenhetService.skjermArkivenhet(
				SkjermArkivenhetRequestTo.builder()
						.arkivenhet(ArkivenhetCode.JOURNALPOST)
						.skjermingType(skjermingType)
						.journalpostId(journalpostId)
						.build());
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har utført slett av journalpost med journalpostId={}", journalpostId);
		return response;
	}

	private void sjekkAbacOgOpprettRequestContext(String journalpostId) {
		abacSecurityService.assertAccessToJournalpostIncludingBegrenset(journalpostId);
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark100");
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
	}

	private void validerOgLagreListeMedAksjonsLoggHeader(String aksjonsLoggHeaderString) throws UgyldigAksjonsLoggHeaderException {
		List<AksjonsLoggHeader> aksjonsLoggHeader = aksjonsLoggHeaderMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString);
		aksjonsLoggService.validateAndSaveAksjon(aksjonsLoggHeader);
	}


	/**
	 * opphevSkjermJournalpost - tidligere angreLogiskSlettDokument(HOVEDDOKUMENT)
	 *
	 * DELETE,
	 * /rest/skjermArkivenhet/pol/journalpost/{journalpostId},
	 * header(aksjonslogg){
	 * 		skjermArkivenhetService.opphevSkjermArkivenhet(pol, journalpost, journalpostId, null, null);
	 * }
	 */


	/**
	 * skjermJournalpostDokumentInfoRelasjon - tidligere logiskSlettDokument(VEDLEGG)
	 *
	 * POST,
	 * /rest/skjermArkivenhet/pol/journalpost_dokument/{journalpostId}/{dokumentInfoId},
	 * header(aksjonslogg){
	 * 		skjermArkivenhetService.skjermArkivenhet(pol, journalpost_dokument, journalpostId, dokumentInfoId, null);
	 * }
	 */


	/**
	 * opphevSkjermJournalpostDokumentInfoRelasjon - tidligere angreLogiskSlettDokument(VEDLEGG)
	 *
	 * DELETE,
	 * /rest/skjermArkivenhet/pol/journalpost_dokument/{journalpostId}/{dokumentInfoId},
	 * header(aksjonslogg){
	 * 		skjermArkivenhetService.opphevSkjermArkivenhet(pol, journalpost_dokument, journalpostId, dokumentInfoId, null);
	 * }
	 */


	/**
	 * skjermDokumentObjekt - tidligere logiskTidligKassasjon
	 *
	 * POST,
	 * /rest/skjermArkivenhet/pol/dokument_objekt/{dokumentInfoId},
	 * header(aksjonslogg){
	 * 		skjermArkivenhetService.skjermArkivenhet(pol, dokument_objekt, dokumentInfoId, null);
	 * }
	 */


	/**
	 * opphevSkjermDokumentObjekt - tidligere angreLogiskTidligKassasjon
	 *
	 * DELETE,
	 * /rest/skjermArkivenhet/pol/dokument_objekt/{dokumentInfoId},
	 * header(aksjonslogg)){
	 * 		skjermArkivenhetService.opphevSkjermArkivenhet(pol, dokument_objekt, dokumentInfoId, null);
	 * }
	 */


	/**
	 * skjermArkivvariant
	 * 	- 	usikker på om dette skal være ett kall her eller vi skal ha tilgant til skjermArkivenhetService
	 * 		i ArkiverKorrigertDokument modulen og skjøte alt derifra.
	 *
	 * 	Hvis kall:
	 * 	POST,
	 * 	/rest/skjermArkivenhet/pol/dokument_objekt/{dokumentInfoId}/{arkivVariant},
	 * 	header(aksjonslogg){
	 * 		skjermArkivenhetService.skjermArkivenhet(pol, dokument_objekt, dokumentInfoId, arkivVariant);
	 *  }
	 *
	 * 	Hvis del av ArkiverKorrigertDokument:
	 * 		skjermArkivenhetService.skjermArkivenhet(pol, dokument_objekt, dokumentInfoId, arkivVariant);
	 */


	/**
	 * opphevSkjermArkivvariant
	 * 	- 	usikker på om dette skal være ett kall her eller vi skal ha tilgant til skjermArkivenhetService
	 * 		i ArkiverKorrigertDokument modulen og skjøte alt derifra.
	 *
	 * 	Hvis kall:
	 * 	DELETE,
	 * 	/rest/skjermArkivenhet/pol/dokument_objekt/{dokumentInfoId}/{arkivVariant},
	 * 	header(aksjonslogg)){
	 * 		skjermArkivenhetService.opphevSkjermArkivenhet(pol, dokument_objekt, dokumentInfoId, arkivVariant);
	 *  }
	 *
	 * 	Hvis del av ArkiverKorrigertDokument:
	 * 		skjermArkivenhetService.opphevSkjermArkivenhet(pol, dokument_objekt, dokumentInfoId, arkivVariant);
	 */


}
