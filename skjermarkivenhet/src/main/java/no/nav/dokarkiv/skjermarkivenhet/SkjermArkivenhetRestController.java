package no.nav.dokarkiv.skjermarkivenhet;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;
import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTOMapper;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.exceptions.UgyldigSkjermArkivenhetRequestException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.skjermarkivenhet.rjoark100.SkjermArkivenhetResponse;
import no.nav.dokarkiv.skjermarkivenhet.rjoark100.SkjermArkivenhetService;
import no.nav.dokarkiv.skjermarkivenhet.rjoark101.OpphevSkjermArkivenhetService;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("rest")
public class SkjermArkivenhetRestController {

	private static final String LOGG_MOTTATT_KALL = " har mottat kall med arkivenhet={}";
	private static final String FEILMELDING_2 = "Validering av input feiler.";        //samle feilmeldinger noe sted?

	private final AbacSecurityService abacSecurityService;
	private final SkjermArkivenhetService skjermArkivenhetService;
	private final OpphevSkjermArkivenhetService opphevSkjermArkivenhetService;
	private final AksjonsLoggService aksjonsLoggService;
	private final AksjonsLoggTOMapper aksjonsLoggTOMapper;


	@Inject
	public SkjermArkivenhetRestController(
			AbacSecurityService abacSecurityService,
			SkjermArkivenhetService skjermArkivenhetService,
			OpphevSkjermArkivenhetService opphevSkjermArkivenhetService,
			AksjonsLoggService aksjonsLoggService) {
		this.abacSecurityService = abacSecurityService;
		this.skjermArkivenhetService = skjermArkivenhetService;
		this.opphevSkjermArkivenhetService = opphevSkjermArkivenhetService;
		this.aksjonsLoggService = aksjonsLoggService;
		this.aksjonsLoggTOMapper = new AksjonsLoggTOMapper();
	}

	@Transactional(rollbackFor = UgyldigAksjonsLoggException.class)
	@ResponseBody
	@PostMapping("/skjermarkivenhet")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark100"}, percentiles = {0.5, 0.95})
	public SkjermArkivenhetResponse skjermArkivenhet(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@RequestBody SkjermArkivenhetRequest skjermArkivenhetRequest) throws UgyldigAksjonsLoggException, UgyldigSkjermArkivenhetRequestException {
		validerAtRequestHarSkjermingOgArkivenhet(skjermArkivenhetRequest.getSkjerming(), skjermArkivenhetRequest.getArkivenhet());
		validerAbac(skjermArkivenhetRequest);
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark100");
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();

		switch (skjermArkivenhetRequest.getArkivenhet()) {
			case JOURNALPOST:
				assertNotNullOrEmpty(skjermArkivenhetRequest.getJournalpostId(), "journalpostId");
				log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + LOGG_MOTTATT_KALL, skjermArkivenhetRequest.getArkivenhet() +
						" og journalpostId={}", skjermArkivenhetRequest.getJournalpostId());
				skjermArkivenhetService.skjermJournalpost(
						skjermArkivenhetRequest.getJournalpostId(), skjermArkivenhetRequest.getSkjerming());
				break;
			case DOKUMENT_INFO:
				assertNotNullOrEmpty(skjermArkivenhetRequest.getJournalpostId(), "journalpostId");
				assertNotNullOrEmpty(skjermArkivenhetRequest.getDokumentInfoId(), "dokumentInfoId");
				log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + LOGG_MOTTATT_KALL, skjermArkivenhetRequest.getArkivenhet() +
								", journalpostId={} og dokumentInfoId={}",
						skjermArkivenhetRequest.getJournalpostId(), skjermArkivenhetRequest.getDokumentInfoId());
				skjermArkivenhetService.skjermDokumentInfo(
						skjermArkivenhetRequest.getJournalpostId(), skjermArkivenhetRequest.getDokumentInfoId(), skjermArkivenhetRequest
								.getSkjerming());
				break;
			case DOKUMENT_FIL:
				assertNotNullOrEmpty(skjermArkivenhetRequest.getDokumentInfoId(), "dokumentInfoId");
				assertNotNullOrEmpty(skjermArkivenhetRequest.getVariant(), "variant");
				log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + LOGG_MOTTATT_KALL, skjermArkivenhetRequest.getArkivenhet() +
						", dokumentInfoId={} og variant={}", skjermArkivenhetRequest.getJournalpostId(), skjermArkivenhetRequest
						.getDokumentInfoId());
				skjermArkivenhetService.skjermDokumentFil(skjermArkivenhetRequest.getDokumentInfoId(), skjermArkivenhetRequest
						.getVariant(), skjermArkivenhetRequest.getSkjerming());
				break;
			default:
				throw new UgyldigSkjermArkivenhetRequestException(FEILMELDING_2 + String.format(
						" Request til skjermArkivenhet inneholder ugyldig arkivenhet. %s er ikke en gyldig verdi for arkivenhet",
						skjermArkivenhetRequest.getArkivenhet()));
		}

		lagreAksjonsLogg(skjermArkivenhetRequest.getJournalpostId(), skjermArkivenhetRequest.getDokumentInfoId(), aksjonsLoggHeaderString, arkivElementEndringTOList);
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har skjermet arkivenhet {}", skjermArkivenhetRequest.getArkivenhet());
		return SkjermArkivenhetResponse.builder()
				.dokumentInfoId(skjermArkivenhetRequest.getDokumentInfoId())
				.journalpostId(skjermArkivenhetRequest.getJournalpostId())
				.variant(skjermArkivenhetRequest.getVariant())
				.build();
	}

	@Transactional(rollbackFor = UgyldigAksjonsLoggException.class)
	@ResponseBody
	@DeleteMapping("/skjermarkivenhet")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark101"}, percentiles = {0.5, 0.95})
	public SkjermArkivenhetResponse opphevSkjermArkivenhet(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@RequestBody SkjermArkivenhetRequest skjermArkivenhetRequest) throws UgyldigAksjonsLoggException, UgyldigSkjermArkivenhetRequestException {
		validerAtRequestHarSkjermingOgArkivenhet(skjermArkivenhetRequest.getSkjerming(), skjermArkivenhetRequest.getArkivenhet());
		validerAbac(skjermArkivenhetRequest);
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark101");
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		//TODO: Fyll inn dette
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();

		switch (skjermArkivenhetRequest.getArkivenhet()) {
			case JOURNALPOST:
				assertNotNullOrEmpty(skjermArkivenhetRequest.getJournalpostId(), "journalpostId");
				log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + LOGG_MOTTATT_KALL, skjermArkivenhetRequest.getArkivenhet() +
						" og journalpostId={}", skjermArkivenhetRequest.getJournalpostId());
				opphevSkjermArkivenhetService.opphevSkjermJournalpost(
						skjermArkivenhetRequest.getJournalpostId(), skjermArkivenhetRequest.getSkjerming());
				break;
			case DOKUMENT_INFO:
				assertNotNullOrEmpty(skjermArkivenhetRequest.getJournalpostId(), "journalpostId");
				assertNotNullOrEmpty(skjermArkivenhetRequest.getDokumentInfoId(), "dokumentInfoId");
				log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + LOGG_MOTTATT_KALL, skjermArkivenhetRequest.getArkivenhet() +
								", journalpostId={} og dokumentInfoId={}",
						skjermArkivenhetRequest.getJournalpostId(), skjermArkivenhetRequest.getDokumentInfoId());
				opphevSkjermArkivenhetService.opphevSkjermDokumentInfo(
						skjermArkivenhetRequest.getJournalpostId(), skjermArkivenhetRequest.getDokumentInfoId(), skjermArkivenhetRequest
								.getSkjerming());
				break;
			case DOKUMENT_FIL:
				assertNotNullOrEmpty(skjermArkivenhetRequest.getDokumentInfoId(), "dokumentInfoId");
				assertNotNullOrEmpty(skjermArkivenhetRequest.getVariant(), "variant");
				log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + LOGG_MOTTATT_KALL, skjermArkivenhetRequest.getArkivenhet() +
						", dokumentInfoId={} og variant={}", skjermArkivenhetRequest.getJournalpostId(), skjermArkivenhetRequest
						.getDokumentInfoId());
				opphevSkjermArkivenhetService.opphevSkjermDokumentFil(skjermArkivenhetRequest.getDokumentInfoId(), skjermArkivenhetRequest
						.getVariant());
				break;
			default:
				throw new UgyldigSkjermArkivenhetRequestException(FEILMELDING_2 + String.format(
						" Request til opphevSkjermArkivenhet inneholder ugyldig arkivenhet. %s er ikke en gyldig verdi for arkivenhet",
						skjermArkivenhetRequest.getArkivenhet()));
		}

		lagreAksjonsLogg(skjermArkivenhetRequest.getJournalpostId(), skjermArkivenhetRequest.getDokumentInfoId(), aksjonsLoggHeaderString, arkivElementEndringTOList);

		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har opphevt skjerming av arkivenhet {}", skjermArkivenhetRequest.getArkivenhet());
		return SkjermArkivenhetResponse.builder().dokumentInfoId(skjermArkivenhetRequest.getDokumentInfoId()).journalpostId(skjermArkivenhetRequest.getJournalpostId()).variant(skjermArkivenhetRequest.getVariant()).build();
	}


	private void validerAtRequestHarSkjermingOgArkivenhet(@NotNull SkjermingTypeCode skjerming, @NotNull ArkivenhetCode
			arkivenhet)
			throws UgyldigSkjermArkivenhetRequestException {
		assertNotNullOrEmpty(skjerming, "skjerming");
		assertNotNullOrEmpty(arkivenhet, "arkivenhet");
	}

	//Gjenbrukt fra AksjonsLoggService men annen exception, legge metoden et annet sted?
	private void assertNotNullOrEmpty(Object value, String parameter) throws UgyldigSkjermArkivenhetRequestException {
		if (Objects.isNull(value) || (value instanceof String && isBlank((String) value))) {
			throw new UgyldigSkjermArkivenhetRequestException(FEILMELDING_2 + " Request til skjermArkivenhet mangler påkrevd parameter: " + parameter);
		}
	}

	private void validerAbac(SkjermArkivenhetRequest skjermArkivenhetRequest) {
		if (skjermArkivenhetRequest.getJournalpostId() == null) {
			abacSecurityService.assertAccessToDokumentIncludingSkjermet(skjermArkivenhetRequest.getDokumentInfoId());
		} else {
			abacSecurityService.assertAccessToJournalpostIncludingBegrenset(skjermArkivenhetRequest.getJournalpostId()
					.toString());
		}
	}

	private void lagreAksjonsLogg(Long journalpostId, Long dokumentInfoId, String aksjonsLoggHeaderString, List<ArkivElementEndringTO> arkivElementEndringTOList) throws
			UgyldigAksjonsLoggException {

		log.info("Lagrer aksjonslogg");
		AksjonsLoggTO aksjonsLoggTO = aksjonsLoggTOMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString, AksjonsTypeCode.ENDRE_SKJERMING, journalpostId, dokumentInfoId);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTO, arkivElementEndringTOList);
	}
}
