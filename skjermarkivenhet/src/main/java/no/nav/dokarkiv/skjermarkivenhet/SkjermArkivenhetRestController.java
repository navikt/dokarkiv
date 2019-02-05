package no.nav.dokarkiv.skjermarkivenhet;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.DELETE_ACTION;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;
import static no.nav.dokarkiv.core.util.ConverterUtils.jsonStringToObjectList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggHeader;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggHeaderMapper;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggHeaderException;
import no.nav.dokarkiv.core.exceptions.UgyldigSkjermArkivenhetHeaderException;
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
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("rest")
public class SkjermArkivenhetRestController {

	private static final String FEILMELDING_2 = "Validering av input feiler.";        //samle feilmeldinger noe sted?

	private final AbacSecurityService abacSecurityService;
	private final SkjermArkivenhetService skjermArkivenhetService;
	private final OpphevSkjermArkivenhetService opphevSkjermArkivenhetService;
	private final AksjonsLoggService aksjonsLoggService;
	private final AksjonsLoggHeaderMapper aksjonsLoggHeaderMapper;


	@Inject
	public SkjermArkivenhetRestController(
			AbacSecurityService abacSecurityService,
			SkjermArkivenhetService skjermArkivenhetService,
			OpphevSkjermArkivenhetService opphevSkjermArkivenhetService,
			AksjonsLoggService aksjonsLoggService,
			AksjonsLoggHeaderMapper aksjonsLoggHeaderMapper) {
		this.abacSecurityService = abacSecurityService;
		this.skjermArkivenhetService = skjermArkivenhetService;
		this.opphevSkjermArkivenhetService = opphevSkjermArkivenhetService;
		this.aksjonsLoggService = aksjonsLoggService;
		this.aksjonsLoggHeaderMapper = aksjonsLoggHeaderMapper;
	}

	@Transactional
	@ResponseBody
	@PostMapping("/skjermarkivenhet")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark100"}, percentiles = {0.5, 0.95})
	public SkjermArkivenhetResponse skjermArkivenhet(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@RequestBody String skjermArkivenhetHeaderString) throws UgyldigAksjonsLoggHeaderException, UgyldigSkjermArkivenhetHeaderException {
		SkjermArkivenhetHeader skjermArkivenhetHeader = validerRequestBody(skjermArkivenhetHeaderString);
		validerAbac(skjermArkivenhetHeader);
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark100");
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		//TODO: Finn ut hva skal logges her. Har ikke alltid journalpostId.
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall med journalpostId={}", skjermArkivenhetHeader.getJournalpostId());
		validerOgLagreListeMedAksjonsLoggHeader(aksjonsLoggHeaderString);
		SkjermArkivenhetResponse response = skjermArkivenhetService.skjermArkivenhet(skjermArkivenhetHeader);
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har skjermet arkivenhet {}", skjermArkivenhetHeader.getArkivenhet());
		return response;
	}

	@Transactional
	@ResponseBody
	@DeleteMapping("/skjermarkivenhet")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = DELETE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark101"}, percentiles = {0.5, 0.95})
	public SkjermArkivenhetResponse opphevSkjermArkivenhet(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@RequestBody String skjermArkivenhetHeaderString) throws UgyldigAksjonsLoggHeaderException, UgyldigSkjermArkivenhetHeaderException {
		SkjermArkivenhetHeader skjermArkivenhetHeader = validerRequestBody(skjermArkivenhetHeaderString);
		validerAbac(skjermArkivenhetHeader);
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark101");
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall med journalpostId={}", skjermArkivenhetHeader.getJournalpostId());
		validerOgLagreListeMedAksjonsLoggHeader(aksjonsLoggHeaderString);
		SkjermArkivenhetResponse response = opphevSkjermArkivenhetService.opphevSkjermArkivenhet(skjermArkivenhetHeader);
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har opphevet skjerming av arkivenhet {}", skjermArkivenhetHeader.getArkivenhet());
		return response;
	}


	private SkjermArkivenhetHeader validerRequestBody(String jsonStringBody) throws UgyldigSkjermArkivenhetHeaderException {
		try {
			List<SkjermArkivenhetHeader> jsonHeaderList = jsonStringToObjectList(jsonStringBody, SkjermArkivenhetHeader.class);
			SkjermArkivenhetHeader skjermArkivenhetHeader = jsonHeaderList.get(0);

			validerSkjermArkivenhetHeader(
					skjermArkivenhetHeader.getSkjerming(),
					skjermArkivenhetHeader.getArkivenhet(),
					skjermArkivenhetHeader.getJournalpostId(),
					skjermArkivenhetHeader.getDokumentInfoId(),
					skjermArkivenhetHeader.getVariant()
			);
			return skjermArkivenhetHeader;
		} catch (IOException e) {
			throw new UgyldigSkjermArkivenhetHeaderException(FEILMELDING_2 + " Sjekk om header er i gyldig JSON format", e);
		}
	}

	private void validerSkjermArkivenhetHeader(
			@NotNull SkjermingTypeCode skjerming,
			@NotNull ArkivenhetCode arkivenhet,
			Long journalpostId,
			Long dokumentInfoId,
			VariantFormatCode variant) throws UgyldigSkjermArkivenhetHeaderException {
		assertNotInvalidEnum(skjerming, "skjerming", SkjermingTypeCode.values());
		assertNotInvalidEnum(arkivenhet, "arkivenhet", ArkivenhetCode.values());

		switch (arkivenhet) {
			case JOURNALPOST:
				assertNotNullOrEmpty(journalpostId, "journalpostId");
				break;
			case DOKUMENT_INFO:
				assertNotNullOrEmpty(journalpostId, "journalpostId");
				assertNotNullOrEmpty(dokumentInfoId, "dokumentInfoId");
				break;
			case DOKUMENT_FIL:
				assertNotNullOrEmpty(dokumentInfoId, "dokumentInfoId");
				assertNotInvalidEnum(variant, "variant", VariantFormatCode.values());
				break;
			default:
				throw new UgyldigSkjermArkivenhetHeaderException(FEILMELDING_2 + String.format(
						" Header til skjermArkivenhet inneholder ugyldig arkivenhet. %s er ikke en gyldig verdi for arkivenhet", arkivenhet));
		}
	}

	//Gjenbrukt fra AksjonsLoggService men annen exception, legge metoden et annet sted?
	private void assertNotInvalidEnum(Object value, String parameter, Enum[] allowedValues) throws UgyldigSkjermArkivenhetHeaderException {
		boolean invalid = true;
		for (Enum e : allowedValues) {
			if (e.name().equals(value)) {
				invalid = false;
				break;
			}
		}

		if (invalid) {
			throw new UgyldigSkjermArkivenhetHeaderException(FEILMELDING_2 + String.format(" Header til skjermArkivenhet inneholder ugyldig verdi: " +
					"%s er ikke en gyldig verdi for %s", value, parameter));
		}
	}

	//Gjenbrukt fra AksjonsLoggService men annen exception, legge metoden et annet sted?
	private void assertNotNullOrEmpty(Object value, String parameter) throws UgyldigSkjermArkivenhetHeaderException {
		if (Objects.isNull(value) || (value instanceof String && isBlank((String) value))) {
			throw new UgyldigSkjermArkivenhetHeaderException(FEILMELDING_2 + " Header til skjermArkivenhet mangler påkrevd parameter: " + parameter);
		}
	}

	private void validerAbac(SkjermArkivenhetHeader skjermArkivenhetHeader) {
		if (skjermArkivenhetHeader.getJournalpostId() == null) {
			abacSecurityService.assertAccessToDokumentIncludingSkjermet(skjermArkivenhetHeader.getDokumentInfoId());
		} else {
			abacSecurityService.assertAccessToJournalpostIncludingBegrenset(skjermArkivenhetHeader.getJournalpostId()
					.toString());
		}
	}

	private void validerOgLagreListeMedAksjonsLoggHeader(String aksjonsLoggHeaderString) throws UgyldigAksjonsLoggHeaderException {
		List<AksjonsLoggHeader> aksjonsLoggHeader = aksjonsLoggHeaderMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString);
		aksjonsLoggService.validateAndSaveAksjon(aksjonsLoggHeader);
	}
}
