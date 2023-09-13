package no.nav.dokarkiv;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigSkjermArkivenhetRequestException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.dto.SkjermArkivenhetRequest;
import no.nav.dokarkiv.rjoark100.SkjermArkivEnhetOrchestrator;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

import static java.util.Objects.isNull;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HJEMMEL_HEADER;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_MELDING_HEADER;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_UTFOERT_AV_HEADER;
import static no.nav.dokarkiv.core.stelvio.RequestContextUtil.createAndSetUsername;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Protected
@RestController
@RequestMapping("rest/admin")
public class SkjermArkivenhetRestController {

	private final SkjermArkivEnhetOrchestrator skjermArkivEnhetOrchestrator;

	public SkjermArkivenhetRestController(SkjermArkivEnhetOrchestrator skjermArkivEnhetOrchestrator) {
		this.skjermArkivEnhetOrchestrator = skjermArkivEnhetOrchestrator;
	}

	@Transactional(rollbackFor = Exception.class)
	@PostMapping("/skjermarkivenhet")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark100a"}, percentiles = {0.5, 0.95})
	public ResponseEntity skjermArkivenhet(
			@RequestHeader(value = AKSJONS_LOGG_HJEMMEL_HEADER) String hjemmel,
			@RequestHeader(value = AKSJONS_LOGG_MELDING_HEADER) String melding,
			@RequestHeader(value = AKSJONS_LOGG_UTFOERT_AV_HEADER, required = false) String utfoertAv,
			@RequestBody SkjermArkivenhetRequest skjermArkivenhetRequest) throws UgyldigSkjermArkivenhetRequestException {

		validerAtRequestHarSkjermingOgArkivenhet(skjermArkivenhetRequest.getSkjerming(), skjermArkivenhetRequest.getArkivenhet());
		MDC.put(MDC_REQUEST_ID, "rjoark100a");
		createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));

		log.info("{} har mottatt kall om å skjerme arkivenhet={} med journalpostId={} og dokumentInfoId={}",
				MDC.get(MDC_REQUEST_ID), skjermArkivenhetRequest.getArkivenhet(), skjermArkivenhetRequest.getJournalpostId(), skjermArkivenhetRequest.getDokumentInfoId());

		skjermArkivEnhetOrchestrator.skjermArkivEnhet(skjermArkivenhetRequest, hjemmel, melding, utfoertAv);
		log.info(MDC.get(MDC_REQUEST_ID) + " har skjermet arkivenhet {}", skjermArkivenhetRequest.getArkivenhet());

		return ResponseEntity.ok().build();
	}

	@Transactional(rollbackFor = Exception.class)
	@DeleteMapping("/skjermarkivenhet")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark100b"}, percentiles = {0.5, 0.95})
	public ResponseEntity opphevSkjermArkivenhet(
			@RequestHeader(value = AKSJONS_LOGG_HJEMMEL_HEADER) String hjemmel,
			@RequestHeader(value = AKSJONS_LOGG_MELDING_HEADER) String melding,
			@RequestHeader(value = AKSJONS_LOGG_UTFOERT_AV_HEADER, required = false) String utfoertAv,
			@RequestBody SkjermArkivenhetRequest skjermArkivenhetRequest) throws UgyldigSkjermArkivenhetRequestException {

		validerAtRequestHarSkjermingOgArkivenhet(skjermArkivenhetRequest.getSkjerming(), skjermArkivenhetRequest.getArkivenhet());
		MDC.put(MDC_REQUEST_ID, "rjoark100b");
		createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));

		log.info("{} har mottatt kall om å oppheve skjerming for arkivenhet={} med journalpostId={} og dokumentInfoId={}", MDC.get(MDC_REQUEST_ID), skjermArkivenhetRequest
				.getArkivenhet(), skjermArkivenhetRequest
				.getJournalpostId(), skjermArkivenhetRequest.getDokumentInfoId());

		skjermArkivEnhetOrchestrator.opphevSkjermArkivEnhet(skjermArkivenhetRequest, hjemmel, melding, utfoertAv);
		log.info(MDC.get(MDC_REQUEST_ID) + " har opphevet skjerming av arkivenhet {}", skjermArkivenhetRequest.getArkivenhet());

		return ResponseEntity.ok().build();
	}

	private void validerAtRequestHarSkjermingOgArkivenhet(@NotNull SkjermingTypeCode skjerming, @NotNull ArkivenhetCode arkivenhet) throws UgyldigSkjermArkivenhetRequestException {
		assertNotNullOrEmpty(skjerming, "skjerming");
		assertNotNullOrEmpty(arkivenhet, "arkivenhet");
	}

	//Gjenbrukt fra AksjonsLoggService men annen exception, legge metoden et annet sted?
	private void assertNotNullOrEmpty(Object value, String parameter) throws UgyldigSkjermArkivenhetRequestException {
		if (isNull(value) || (value instanceof String && isBlank((String) value))) {
			throw new UgyldigSkjermArkivenhetRequestException("Validering av input feilet: Kallet mangler påkrevd parameter: " + parameter);
		}
	}

}
