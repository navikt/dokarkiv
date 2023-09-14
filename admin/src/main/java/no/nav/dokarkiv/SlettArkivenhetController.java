package no.nav.dokarkiv;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.dto.SlettArkivenhetRequest;
import no.nav.dokarkiv.rjoark101.SlettArkivenhetOrchestrator;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HJEMMEL_HEADER;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_MELDING_HEADER;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_UTFOERT_AV_HEADER;
import static no.nav.dokarkiv.core.stelvio.RequestContextUtil.createAndSetUsername;

@Slf4j
@Protected
@RestController
@RequestMapping("rest/admin")
public class SlettArkivenhetController {

	private final SlettArkivenhetOrchestrator slettArkivenhetOrchestrator;

	public SlettArkivenhetController(SlettArkivenhetOrchestrator slettArkivenhetOrchestrator) {
		this.slettArkivenhetOrchestrator = slettArkivenhetOrchestrator;
	}

	@Transactional(rollbackFor = Exception.class)
	@DeleteMapping("/slettarkivenhet")
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark101"}, percentiles = {0.5, 0.95})
	public ResponseEntity slettArkivenhet(
			@RequestHeader(value = AKSJONS_LOGG_HJEMMEL_HEADER) String hjemmel,
			@RequestHeader(value = AKSJONS_LOGG_MELDING_HEADER, required = false) String melding,
			@RequestHeader(value = AKSJONS_LOGG_UTFOERT_AV_HEADER, required = false) String utfoertAv,
			@RequestBody SlettArkivenhetRequest slettArkivenhetRequest) {

		MDC.put(MDC_REQUEST_ID, "rjoark101");
		log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall om sletting av arkivenhet={} med journalpostId={}, dokumentInfoId={} og variant={}",
				slettArkivenhetRequest.getArkivenhet(), slettArkivenhetRequest.getJournalpostId(), slettArkivenhetRequest.getDokumentInfoId(), slettArkivenhetRequest.getVariant());
		createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));

		slettArkivenhetOrchestrator.slettArkivenhhet(slettArkivenhetRequest.getArkivenhet(), slettArkivenhetRequest.getJournalpostId(), slettArkivenhetRequest
				.getDokumentInfoId(), slettArkivenhetRequest.getVariant(), hjemmel, melding, utfoertAv);
		log.info(MDC.get(MDC_REQUEST_ID) + " har slettet arkivenhet={} med journalpostId={}, dokumentInfoId={} og variant={}",
				slettArkivenhetRequest.getArkivenhet(), slettArkivenhetRequest.getJournalpostId(), slettArkivenhetRequest.getDokumentInfoId(), slettArkivenhetRequest.getVariant());

		return ResponseEntity.ok().build();
	}

}