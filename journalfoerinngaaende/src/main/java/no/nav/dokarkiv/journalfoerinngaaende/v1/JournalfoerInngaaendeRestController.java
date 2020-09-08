package no.nav.dokarkiv.journalfoerinngaaende.v1;


import lombok.extern.slf4j.Slf4j;
import no.nav.dok.tjenester.journalfoerinngaaende.GetJournalpostResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.PostLogiskVedleggRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PostLogiskVedleggResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.PutDokumentRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutDokumentResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.PutLogiskVedleggRequest;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalfoerinngaaende.v1.rjoark001i.GetInngaaendeJournalpostService;
import no.nav.dokarkiv.journalfoerinngaaende.v1.rjoark002i.UpdateInngaaendeJournalpostService;
import no.nav.dokarkiv.journalfoerinngaaende.v1.rjoark003i.UpdateInngaaendeJournalpostDokumentService;
import no.nav.dokarkiv.journalfoerinngaaende.v1.rjoark004i.LogiskVedleggService;
import no.nav.dokarkiv.journalfoerinngaaende.v1.util.Utils;
import no.nav.freg.abac.core.annotation.Abac;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.CREATE_ACTION;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.DELETE_ACTION;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.READ_ACTION;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;


/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@Protected
@RestController
@RequestMapping("/rest/journalfoerinngaaende/v1/journalposter")
public class JournalfoerInngaaendeRestController {

	private final GetInngaaendeJournalpostService getInngaaendeJournalpostService;
	private final UpdateInngaaendeJournalpostService updateInngaaendeJournalpostService;
	private final LogiskVedleggService logiskVedleggService;
	private final AbacSecurityService abacSecurityService;
	private final UpdateInngaaendeJournalpostDokumentService updateInngaaendeJournalpostDokumentService;

	@Inject
	public JournalfoerInngaaendeRestController(GetInngaaendeJournalpostService getInngaaendeJournalpostService,
											   UpdateInngaaendeJournalpostService updateInngaaendeJournalpostService,
											   LogiskVedleggService logiskVedleggService,
											   AbacSecurityService abacSecurityService,
											   UpdateInngaaendeJournalpostDokumentService updateInngaaendeJournalpostDokumentService) {
		this.getInngaaendeJournalpostService = getInngaaendeJournalpostService;
		this.abacSecurityService = abacSecurityService;
		this.updateInngaaendeJournalpostDokumentService = updateInngaaendeJournalpostDokumentService;
		this.updateInngaaendeJournalpostService = updateInngaaendeJournalpostService;
		this.logiskVedleggService = logiskVedleggService;
	}

	@Transactional(readOnly = true)
	@ResponseBody
	@GetMapping(value = "/{journalpostId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)},
			actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark001i"}, percentiles = {0.5, 0.95})
	public GetJournalpostResponse getInngaaendeJournalpost(@PathVariable String journalpostId) {
		log.info("rjoark001i har mottatt kall om å hente journalpost med journalpostId={} fra Joark.", journalpostId);
		Utils.validateId(journalpostId, "journalpostId");
		abacSecurityService.assertAccessToJournalpost(journalpostId);
		GetJournalpostResponse responseTo = getInngaaendeJournalpostService.getInngaaendeJournalpostByJournalpostId(journalpostId);
		log.info("rjoark001i har hentet journalpost med journalpostId={} og dokumentinfoId(er)={} fra Joark.",
				journalpostId, Utils.getDokumentIds(responseTo));
		return responseTo;
	}

	@Transactional
	@ResponseBody
	@PutMapping(value = "/{journalpostId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark002i"}, percentiles = {0.5, 0.95})
	public PutJournalpostResponse updateInngaaendeJournalpost(@PathVariable String journalpostId, @RequestBody PutJournalpostRequest request) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		log.info(String.format("rjoark002i har mottatt kall om å oppdatere inngaaende journalpost med journalpostId=%s", journalpostId));
		Utils.validateId(journalpostId, "journalpostId");
		abacSecurityService.assertAccessToJournalpost(journalpostId);
		PutJournalpostResponse responseTo = updateInngaaendeJournalpostService.updateInngaaendeJournalpost(journalpostId, request);
		log.info("rjoark002i har oppdatert journalpost med journalpostId={} i Joark.", journalpostId);
		return responseTo;
	}

	@Transactional
	@ResponseBody
	@PutMapping(value = "/{journalpostId}/dokumenter/{dokumentId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark003i"}, percentiles = {0.5, 0.95})
	public PutDokumentResponse updateDokument(@PathVariable String journalpostId, @PathVariable String dokumentId, @RequestBody PutDokumentRequest request) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		log.info("rjoark003i har mottat kall om å oppdatere dokument med journalpostId={} og dokumentId={}", journalpostId, dokumentId);
		Utils.validateJournalpostIdAndDokumentId(journalpostId, dokumentId);
		abacSecurityService.assertAccessToJournalpost(journalpostId);
		PutDokumentResponse responseTo = updateInngaaendeJournalpostDokumentService.update(journalpostId, dokumentId, request);
		log.info("rjoark003i har oppdatert dokument med journalpostId={} og dokumentId={} i Joark.", journalpostId, dokumentId);
		return responseTo;
	}

	@Transactional
	@ResponseBody
	@PostMapping(value = "/{journalpostId}/dokumenter/{dokumentId}/logiskeVedlegg")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)},
			actions = @Abac.Attr(key = ACTION_ID, value = CREATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark004i_post"}, percentiles = {0.5, 0.95})
	public PostLogiskVedleggResponse persistLogiskVedlegg(@PathVariable String journalpostId, @PathVariable String dokumentId, @RequestBody PostLogiskVedleggRequest request) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		log.info("rjoark004i har mottatt kall om å persistere logisk vedlegg på journalpost med journalpostId={} og dokumentId={}", journalpostId, dokumentId);
		Utils.validateJournalpostIdAndDokumentId(journalpostId, dokumentId);
		abacSecurityService.assertAccessToJournalpost(journalpostId);
		PostLogiskVedleggResponse responseTo = logiskVedleggService.persistLogiskVedlegg(journalpostId, dokumentId, request);
		log.info("rjoark004i persisterte logisk vedlegg med logiskVedleggId={}. journalpostId={}, dokumentId=%{}",
				responseTo.getLogiskVedleggId(), journalpostId, dokumentId);
		return responseTo;
	}

	@Transactional
	@ResponseBody
	@PutMapping(value = "/{journalpostId}/dokumenter/{dokumentId}/logiskeVedlegg/{logiskVedleggId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark004i_put"}, percentiles = {0.5, 0.95})
	public String updateLogiskVedlegg(@PathVariable String journalpostId, @PathVariable String dokumentId, @PathVariable String logiskVedleggId, @RequestBody PutLogiskVedleggRequest request) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		log.info("rjoark004i har mottatt kall om å oppdatere logisk vedlegg med logiskVedleggId={} på journalpost med journalpostId={} og dokumentId={}", logiskVedleggId, journalpostId, dokumentId);
		Utils.validateIds(journalpostId, dokumentId, logiskVedleggId);
		abacSecurityService.assertAccessToJournalpost(journalpostId);
		logiskVedleggService.updateLogiskVedlegg(journalpostId, dokumentId, logiskVedleggId, request);
		log.info("rjoark004i oppdaterte logisk vedlegg på journalpost, journalpostId={}, dokumentinfoId={}, logiskVedleggId={}.", journalpostId, dokumentId, logiskVedleggId);
		return String.format("Oppdatering av logiskVedlegg med logiskVedleggId=%s var vellykket. journalpostId=%s, dokumentId=%s",
				logiskVedleggId, journalpostId, dokumentId);
	}

	@Transactional
	@ResponseBody
	@DeleteMapping(value = "/{journalpostId}/dokumenter/{dokumentId}/logiskeVedlegg/{logiskVedleggId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)},
			actions = @Abac.Attr(key = ACTION_ID, value = DELETE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark004i_delete"}, percentiles = {0.5, 0.95})
	public String deleteLogiskVedlegg(@PathVariable String journalpostId, @PathVariable String dokumentId, @PathVariable String logiskVedleggId) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		log.info("rjoark004i delete har mottatt kall om å slette logisk vedlegg med logiskVedleggId={} fra journalpost med journalpostId={} og dokumentId={}", logiskVedleggId, journalpostId, dokumentId);
		Utils.validateIds(journalpostId, dokumentId, logiskVedleggId);
		abacSecurityService.assertAccessToJournalpost(journalpostId);
		logiskVedleggService.deleteLogiskVedlegg(journalpostId, dokumentId, logiskVedleggId);
		log.info("rjoark004i har slettet logisk vedlegg fra journalpost, journalpostId={}, dokumentinfoId={}, logiskVedleggId={}.", journalpostId, dokumentId, logiskVedleggId);
		return String.format("Sleting av logiskVedlegg med logiskVedleggId=%s var vellykket. journalpostId=%s, dokumentId=%s",
				logiskVedleggId, journalpostId, dokumentId);
	}

}
