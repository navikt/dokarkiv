package no.nav.dokarkiv.logiskslettdokument;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentRequestTo;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentResponse;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentService;
import no.nav.dokarkiv.logiskslettdokument.rjoark101.AngreLogiskSlettDokumentService;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@Slf4j
@RestController
@RequestMapping("rest/logiskslettdokument")
public class LogiskSlettDokumentRestController {

	private final LogiskSlettDokumentService logiskSlettDokumentService;
	private final AngreLogiskSlettDokumentService angreLogiskSlettDokumentService;
	private final AbacSecurityService abacSecurityService;

	@Inject
	public LogiskSlettDokumentRestController(LogiskSlettDokumentService logiskSlettDokumentService,
											 AngreLogiskSlettDokumentService angreLogiskSlettDokumentService,
											 AbacSecurityService abacSecurityService) {
		this.logiskSlettDokumentService = logiskSlettDokumentService;
		this.angreLogiskSlettDokumentService = angreLogiskSlettDokumentService;
		this.abacSecurityService = abacSecurityService;
	}


	@Transactional
	@ResponseBody
	@PatchMapping("/{journalpostId}/{dokumentInfoId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark100"}, percentiles = {0.5, 0.95})
	public LogiskSlettDokumentResponse logiskSlettDokumentKnyttetKunEnJournalpost(
			@PathVariable("journalpostId") Long journalpostId, @PathVariable("dokumentInfoId") Long dokumentInfoId) {
		abacSecurityService.assertAccessToJournalpostIncludingBegrenset(journalpostId.toString());
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark100");
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall med journalpostId=" + journalpostId + " og dokumentInfoId=" + dokumentInfoId);
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		return logiskSlettDokumentService.logiskSlettDokumentKnyttetKunEnJournalpost(LogiskSlettDokumentRequestTo.builder()
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.build());
	}

	@Transactional
	@ResponseBody
	@PatchMapping("/angre/{journalpostId}/{dokumentInfoId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark101"}, percentiles = {0.5, 0.95})
	public LogiskSlettDokumentResponse angreLogiskSlettDokument(@PathVariable("journalpostId") Long journalpostId,
																@PathVariable("dokumentInfoId") Long dokumentInfoId) {
		abacSecurityService.assertAccessToJournalpostIncludingBegrenset(journalpostId.toString());
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark101");
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall med journalpostId=" + journalpostId + " og dokumentInfoId=" + dokumentInfoId);
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		return angreLogiskSlettDokumentService.angreLogiskSlettDokument(LogiskSlettDokumentRequestTo.builder()
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.build());
	}

}