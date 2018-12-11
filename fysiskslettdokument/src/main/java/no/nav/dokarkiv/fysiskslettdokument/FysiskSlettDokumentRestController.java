package no.nav.dokarkiv.fysiskslettdokument;


import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.fysiskslettdokument.rjoark102.FysiskSlettDokumentRequestTo;
import no.nav.dokarkiv.fysiskslettdokument.rjoark102.FysiskSlettDokumentResponse;
import no.nav.dokarkiv.fysiskslettdokument.rjoark102.FysiskSlettDokumentService;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@Slf4j
@RestController
@RequestMapping("rest/fysiskslettdokument")
public class FysiskSlettDokumentRestController {

	private final FysiskSlettDokumentService fysiskSlettDokumentService;
	private final AbacSecurityService abacSecurityService;

	@Inject
	public FysiskSlettDokumentRestController(FysiskSlettDokumentService fysiskSlettDokumentService,
											 AbacSecurityService abacSecurityService) {
		this.fysiskSlettDokumentService = fysiskSlettDokumentService;
		this.abacSecurityService = abacSecurityService;
	}

	@Transactional
	@ResponseBody
	@DeleteMapping("/{journalpostId}/{dokumentInfoId}/{begrensningType}")
	//TODO: MÅ endre fra value = UPDATE_ACTION til DELETE_ACTION. Men joarkadmin har ikke tilgang.
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark102"}, percentiles = {0.5, 0.95})
	public FysiskSlettDokumentResponse fysiskSlettDokument(
			@PathVariable("journalpostId") Long journalpostId,
			@PathVariable("dokumentInfoId") Long dokumentInfoId,
			@PathVariable("begrensningType") BegrensningTypeCode begrensningType) {
		abacSecurityService.assertAccessToJournalpostIncludingBegrenset(journalpostId.toString());
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark102");
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall med journalpostId=" + journalpostId + ", dokumentInfoId=" + dokumentInfoId + " og begrensningType=" + begrensningType);
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		return fysiskSlettDokumentService.sletteDokumentFysisk(FysiskSlettDokumentRequestTo.builder()
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.begrensningType(begrensningType)
				.build());
	}

}
