package no.nav.dokarkiv.arkiverkorrigertdokument;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103.ArkiverKorrigertDokumentRequestTo;
import no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103.ArkiverKorrigertDokumentService;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@Slf4j
@RestController
@RequestMapping("rest/arkiverkorrigertdokument")
public class ArkiverKorrigertDokumentRestController {

	private final ArkiverKorrigertDokumentService arkiverKorrigertDokumentService;
	private final AbacSecurityService abacSecurityService;

	@Inject
	public ArkiverKorrigertDokumentRestController(
			ArkiverKorrigertDokumentService arkiverKorrigertDokumentService,
			AbacSecurityService abacSecurityService) {
		this.arkiverKorrigertDokumentService = arkiverKorrigertDokumentService;
		this.abacSecurityService = abacSecurityService;
	}

	@Transactional
	@ResponseBody
	@PostMapping("/{journalpostId}/{dokumentInfoId}")
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark103"}, percentiles = {0.5, 0.95})
	public String arkiverKorrigertDokument(@PathVariable("journalpostId") Long journalpostId,
										   @PathVariable("dokumentInfoId") Long dokumentInfoId,
										   @PathVariable("dokumentInfoId") byte[] body) {
		abacSecurityService.assertAccessToJournalpostIncludingBegrenset(journalpostId.toString());
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark103");
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall med journalpostId={} og dokumentInfoId={}",
				journalpostId, dokumentInfoId);
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		return arkiverKorrigertDokumentService.arkiverKorrigertDokument(ArkiverKorrigertDokumentRequestTo.builder()
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.build());
	}

}
