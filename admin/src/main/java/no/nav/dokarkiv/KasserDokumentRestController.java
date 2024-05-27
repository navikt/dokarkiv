package no.nav.dokarkiv;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.dto.KasserDokumentRequest;
import no.nav.dokarkiv.rjoark102.KasserDokumentOrchestrator;
import no.nav.dokarkiv.rjoark102.KasserDokumentValidator;
import no.nav.security.token.support.core.api.Protected;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.dokarkiv.AdminConstants.JOARKADMIN_ROLE_CLAIM_TILGANG;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HJEMMEL_HEADER;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_MELDING_HEADER;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_UTFOERT_AV_HEADER;
import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.core.stelvio.RequestContextUtil.createAndSetUsername;

@Slf4j
@Protected
@RestController
@RequestMapping("rest/admin")
@ProtectedWithClaims(issuer = ISSUER_AZUREV2, claimMap = {"roles=" + JOARKADMIN_ROLE_CLAIM_TILGANG, "scp=" + JOARKADMIN_ROLE_CLAIM_TILGANG}, combineWithOr=true)
public class KasserDokumentRestController {

	private final KasserDokumentValidator validator;
	private final KasserDokumentOrchestrator kasserDokumentOrchestrator;

	public KasserDokumentRestController(KasserDokumentValidator validator,
										KasserDokumentOrchestrator kasserDokumentOrchestrator) {
		this.validator = validator;
		this.kasserDokumentOrchestrator = kasserDokumentOrchestrator;
	}

	@Transactional(rollbackFor = Exception.class)
	@DeleteMapping("/kasserdokument")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark102"}, percentiles = {0.5, 0.95})
	public ResponseEntity kasserDokument(
			@RequestHeader(value = AKSJONS_LOGG_HJEMMEL_HEADER) String hjemmel,
			@RequestHeader(value = AKSJONS_LOGG_MELDING_HEADER, required = false) String melding,
			@RequestHeader(value = AKSJONS_LOGG_UTFOERT_AV_HEADER, required = false) String utfoertAv,
			@RequestBody KasserDokumentRequest request) {
		MDC.put(MDC_REQUEST_ID, "rjoark102");

		validator.validerKasserDokumentRequest(request);
		log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall med dokumentInfoId={}", request.getDokumentInfoId());

		createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));

		kasserDokumentOrchestrator.kasserDokument(request.getDokumentInfoId(), request.getKassertAvNavn(), hjemmel, melding, utfoertAv);
		log.info("{} har kassert dokument med dokumentInfoId={}", MDC.get(MDC_REQUEST_ID), request.getDokumentInfoId());

		return ResponseEntity.ok().build();
	}

	@Transactional(rollbackFor = Exception.class)
	@PostMapping("/kasserdokument/skjerm/{dokumentInfoId}")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark102SA"}, percentiles = {0.5, 0.95})
	public ResponseEntity kasserSkjermDokument(
			@RequestHeader(value = AKSJONS_LOGG_HJEMMEL_HEADER) String hjemmel,
			@RequestHeader(value = AKSJONS_LOGG_MELDING_HEADER) String melding,
			@RequestHeader(value = AKSJONS_LOGG_UTFOERT_AV_HEADER, required = false) String utfoertAv,
			@PathVariable Long dokumentInfoId) {
		MDC.put(MDC_REQUEST_ID, "rjoark102S");

		log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall med dokumentInfoId={}", dokumentInfoId);
		createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));

		kasserDokumentOrchestrator.kasserSkjermDokument(dokumentInfoId, hjemmel, melding, utfoertAv);
		log.info("{} har skjermet dokument for kassering med dokumentInfoId={}", MDC.get(MDC_REQUEST_ID), dokumentInfoId);

		return ResponseEntity.ok().build();
	}

	@Transactional(rollbackFor = Exception.class)
	@DeleteMapping("/kasserdokument/skjerm/{dokumentInfoId}")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark102SB"}, percentiles = {0.5, 0.95})
	public ResponseEntity opphevKasserSkjermDokument(
			@RequestHeader(value = AKSJONS_LOGG_HJEMMEL_HEADER) String hjemmel,
			@RequestHeader(value = AKSJONS_LOGG_MELDING_HEADER) String melding,
			@RequestHeader(value = AKSJONS_LOGG_UTFOERT_AV_HEADER, required = false) String utfoertAv,
			@PathVariable Long dokumentInfoId) {
		MDC.put(MDC_REQUEST_ID, "rjoark102SB");

		log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall med dokumentInfoId={}", dokumentInfoId);
		createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));

		kasserDokumentOrchestrator.opphevKasserSkjermDokument(dokumentInfoId, hjemmel, melding, utfoertAv);
		log.info("{} har opphevet skjerming for dokument som var skjermet som kassert med dokumentInfoId={}", MDC.get(MDC_REQUEST_ID), dokumentInfoId);

		return ResponseEntity.ok().build();
	}
}
