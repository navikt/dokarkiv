package no.nav.dokarkiv;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;
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
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.exceptions.UgyldigSkjermArkivenhetRequestException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.dto.SkjermArkivenhetRequest;
import no.nav.dokarkiv.dto.SkjermArkivenhetResponse;
import no.nav.dokarkiv.rjoark100.OpphevSkjermArkivenhetService;
import no.nav.dokarkiv.rjoark100.SkjermArkivenhetService;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("rest")
public class SkjermArkivenhetRestController {

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
	public ResponseEntity<SkjermArkivenhetResponse> skjermArkivenhet(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@RequestBody SkjermArkivenhetRequest skjermArkivenhetRequest) throws UgyldigAksjonsLoggException, UgyldigSkjermArkivenhetRequestException {
		validerAtRequestHarSkjermingOgArkivenhet(skjermArkivenhetRequest.getSkjerming(), skjermArkivenhetRequest.getArkivenhet());
		validerAbac(skjermArkivenhetRequest);
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark100");
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		log.info("{} har mottat kall om å skjerme arkivenhet={} med journalpostId={} og dokumentInfoId={}", MDC.get(MDCConstants.MDC_REQUEST_ID), skjermArkivenhetRequest
				.getJournalpostId(), skjermArkivenhetRequest.getDokumentInfoId());
		switch (skjermArkivenhetRequest.getArkivenhet()) {
			case JOURNALPOST:
				assertNotNullOrEmpty(skjermArkivenhetRequest.getJournalpostId(), "journalpostId");
				skjermArkivenhetService.skjermJournalpost(
						skjermArkivenhetRequest.getJournalpostId(), skjermArkivenhetRequest.getSkjerming());
				break;
			case VEDLEGG:
				assertNotNullOrEmpty(skjermArkivenhetRequest.getJournalpostId(), "journalpostId");
				assertNotNullOrEmpty(skjermArkivenhetRequest.getDokumentInfoId(), "dokumentInfoId");
				skjermArkivenhetService.skjermVedlegg(
						skjermArkivenhetRequest.getJournalpostId(), skjermArkivenhetRequest.getDokumentInfoId(), skjermArkivenhetRequest
								.getSkjerming());
				break;
			case DOKUMENT_FIL:
				assertNotNullOrEmpty(skjermArkivenhetRequest.getDokumentInfoId(), "dokumentInfoId");
				assertNotNullOrEmpty(skjermArkivenhetRequest.getVariant(), "variant");
				skjermArkivenhetService.skjermDokumentFil(skjermArkivenhetRequest.getDokumentInfoId(), skjermArkivenhetRequest
						.getVariant(), skjermArkivenhetRequest.getSkjerming());
		}

		List<ArkivElementEndringTO> arkivElementEndringTOList = createArkivElementEndringTO(skjermArkivenhetRequest.getArkivenhet(), skjermArkivenhetRequest
				.getVariant(), null, skjermArkivenhetRequest.getSkjerming().name());
		lagreAksjonsLogg(skjermArkivenhetRequest.getJournalpostId(), skjermArkivenhetRequest.getDokumentInfoId(), aksjonsLoggHeaderString, arkivElementEndringTOList);

		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har skjermet arkivenhet {}", skjermArkivenhetRequest.getArkivenhet());
		return ResponseEntity
				.ok()
				.body(SkjermArkivenhetResponse.builder()
						.dokumentInfoId(skjermArkivenhetRequest.getDokumentInfoId())
						.journalpostId(skjermArkivenhetRequest.getJournalpostId())
						.variant(skjermArkivenhetRequest.getVariant())
						.build());
	}

	@Transactional(rollbackFor = UgyldigAksjonsLoggException.class)
	@ResponseBody
	@DeleteMapping("/skjermarkivenhet")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark100"}, percentiles = {0.5, 0.95})
	public SkjermArkivenhetResponse opphevSkjermArkivenhet(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@RequestBody SkjermArkivenhetRequest skjermArkivenhetRequest) throws UgyldigAksjonsLoggException, UgyldigSkjermArkivenhetRequestException {
		validerAtRequestHarSkjermingOgArkivenhet(skjermArkivenhetRequest.getSkjerming(), skjermArkivenhetRequest.getArkivenhet());
		validerAbac(skjermArkivenhetRequest);
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark100");

		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		log.info("{} har mottat kall om å oppheve skjerming for arkivenhet={} med journalpostId={} og dokumentInfoId={}", MDC.get(MDCConstants.MDC_REQUEST_ID), skjermArkivenhetRequest
				.getJournalpostId(), skjermArkivenhetRequest.getDokumentInfoId());

		switch (skjermArkivenhetRequest.getArkivenhet()) {
			case JOURNALPOST:
				assertNotNullOrEmpty(skjermArkivenhetRequest.getJournalpostId(), "journalpostId");
				opphevSkjermArkivenhetService.opphevSkjermJournalpost(
						skjermArkivenhetRequest.getJournalpostId(), skjermArkivenhetRequest.getSkjerming());
				break;
			case VEDLEGG:
				assertNotNullOrEmpty(skjermArkivenhetRequest.getJournalpostId(), "journalpostId");
				assertNotNullOrEmpty(skjermArkivenhetRequest.getDokumentInfoId(), "dokumentInfoId");
				opphevSkjermArkivenhetService.opphevSkjermDokumentInfo(
						skjermArkivenhetRequest.getJournalpostId(), skjermArkivenhetRequest.getDokumentInfoId(), skjermArkivenhetRequest
								.getSkjerming());
				break;
			case DOKUMENT_FIL:
				assertNotNullOrEmpty(skjermArkivenhetRequest.getDokumentInfoId(), "dokumentInfoId");
				assertNotNullOrEmpty(skjermArkivenhetRequest.getVariant(), "variant");
				opphevSkjermArkivenhetService.opphevSkjermDokumentFil(skjermArkivenhetRequest.getDokumentInfoId(), skjermArkivenhetRequest
						.getVariant());
		}

		List<ArkivElementEndringTO> arkivElementEndringTOList = createArkivElementEndringTO(skjermArkivenhetRequest.getArkivenhet(), skjermArkivenhetRequest
				.getVariant(), skjermArkivenhetRequest.getSkjerming().name(), null);
		lagreAksjonsLogg(skjermArkivenhetRequest.getJournalpostId(), skjermArkivenhetRequest.getDokumentInfoId(), aksjonsLoggHeaderString, arkivElementEndringTOList);

		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har opphevet skjerming av arkivenhet {}", skjermArkivenhetRequest.getArkivenhet());
		return SkjermArkivenhetResponse.builder()
				.dokumentInfoId(skjermArkivenhetRequest.getDokumentInfoId())
				.journalpostId(skjermArkivenhetRequest.getJournalpostId())
				.variant(skjermArkivenhetRequest.getVariant())
				.build();
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
			throw new UgyldigSkjermArkivenhetRequestException("Validering av input feilet: Kallet mangler påkrevd parameter: " + parameter);
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

		AksjonsLoggTO aksjonsLoggTO = aksjonsLoggTOMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString, AksjonsTypeCode.ENDRE_SKJERMING, journalpostId, dokumentInfoId);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTO, arkivElementEndringTOList);
	}

	private List<ArkivElementEndringTO> createArkivElementEndringTO(ArkivenhetCode arkivenhetCode, VariantFormatCode variantFormatCode, String fraVerdi, String tilVerdi) {
		ArkivElementEndringTO.ArkivElementEndringTOBuilder arkivElementEndringTO = ArkivElementEndringTO.builder();

		switch (arkivenhetCode) {
			case JOURNALPOST:
				arkivElementEndringTO.arkivElement(JOURNALPOST_SKJERMING_TYPE);
				break;
			case VEDLEGG:
				arkivElementEndringTO.arkivElement(RELASJON_SKJERMING_TYPE);
				break;
			case DOKUMENT_FIL:
				arkivElementEndringTO.arkivElement(String.format("Fildetaljer.variantFormat[%s].skjermingType", variantFormatCode
						.name()));
				break;
		}

		arkivElementEndringTO.fraVerdi(fraVerdi);
		arkivElementEndringTO.tilVerdi(tilVerdi);

		return Arrays.asList(arkivElementEndringTO.build());
	}
}
