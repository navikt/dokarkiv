package no.nav.dokarkiv;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTOMapper;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.dto.SlettArkivenhetRequest;
import no.nav.dokarkiv.dto.SlettArkivenhetResponse;
import no.nav.dokarkiv.rjoark101.SlettArkivenhetOrchestrator;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Slf4j
@RestController
@RequestMapping("rest/admin")
public class SlettArkivenhetController {


	private final SlettArkivenhetOrchestrator slettArkivenhetOrchestrator;
	private final AksjonsLoggService aksjonsLoggService;
	private final AksjonsLoggTOMapper aksjonsLoggTOMapper;

	public SlettArkivenhetController(SlettArkivenhetOrchestrator slettArkivenhetOrchestrator, AksjonsLoggService aksjonsLoggService) {
		this.slettArkivenhetOrchestrator = slettArkivenhetOrchestrator;
		this.aksjonsLoggService = aksjonsLoggService;
		this.aksjonsLoggTOMapper = new AksjonsLoggTOMapper();
	}

	@Transactional(rollbackFor = Exception.class)
	@DeleteMapping("/slettarkivenhet")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark101"}, percentiles = {0.5, 0.95})
	public SlettArkivenhetResponse slettArkivenhet(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@RequestBody SlettArkivenhetRequest slettArkivenhetRequest) throws UgyldigAksjonsLoggException {
		//TODO: Abac security
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark101");
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall om sletting av arkivenhet={} med journalpostId={}, dokumentInfoId={} og variant={}",
				slettArkivenhetRequest.getArkivenhet(), slettArkivenhetRequest.getJournalpostId(), slettArkivenhetRequest.getDokumentInfoId(), slettArkivenhetRequest
						.getVariant());
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		slettArkivenhetOrchestrator.slettArkivenhhet(slettArkivenhetRequest.getArkivenhet(), slettArkivenhetRequest.getJournalpostId(), slettArkivenhetRequest
				.getDokumentInfoId(), slettArkivenhetRequest.getVariant(), aksjonsLoggHeaderString);

		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har slettet arkivenhet={} med journalpostId={}, dokumentInfoId={} og variant={}",
				slettArkivenhetRequest.getArkivenhet(), slettArkivenhetRequest.getJournalpostId(), slettArkivenhetRequest.getDokumentInfoId(), slettArkivenhetRequest
						.getVariant());
		return SlettArkivenhetResponse.builder()
				.dokumentInfoId(slettArkivenhetRequest.getDokumentInfoId())
				.journalpostId(slettArkivenhetRequest.getJournalpostId())
				.build();

	}


}
