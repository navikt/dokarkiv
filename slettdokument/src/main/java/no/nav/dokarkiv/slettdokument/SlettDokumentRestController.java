package no.nav.dokarkiv.slettdokument;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.slettdokument.service.SlettDokumentRestService;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@Slf4j
@RestController
@RequestMapping("/rest/slettdokument")
public class SlettDokumentRestController {


	private final SlettDokumentRestService slettDokumentRestService;
	private final AbacSecurityService abacSecurityService;

	@Inject
	public SlettDokumentRestController(SlettDokumentRestService slettDokumentRestService,
									   AbacSecurityService abacSecurityService) {
		this.slettDokumentRestService = slettDokumentRestService;
		this.abacSecurityService = abacSecurityService;
	}


	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@DeleteMapping("/{journalpostId}/{dokumentInfoId}")
	public String deleteDocumentWithJournalpostIdAndDokumentInfoId(@PathVariable("journalpostId") Long journalpostId, @PathVariable("dokumentInfoId") Long dokumentInfoId) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		slettDokumentRestService.slettDokument(journalpostId, dokumentInfoId);
		return "OK";
	}

		@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@DeleteMapping("/{journalpostId}")
	public String deleteDocumentWithJournalpostId(@PathVariable("journalpostId") Long journalpostId) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		slettDokumentRestService.slettDokumentMedJournalpostId(journalpostId);
		return "OK";
	}
}