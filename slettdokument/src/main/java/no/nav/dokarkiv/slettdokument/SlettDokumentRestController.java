package no.nav.dokarkiv.slettdokument;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.slettdokument.service.SlettDokumentRequestTo;
import no.nav.dokarkiv.slettdokument.service.SlettDokumentService;
import no.nav.dokarkiv.slettdokument.util.Utils;
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
@RequestMapping("rest/slettdokument")
public class SlettDokumentRestController {

	public static final String REQUEST_ID = "slettdokument";

	private final SlettDokumentService slettDokumentService;
	private final AbacSecurityService abacSecurityService;

	@Inject
	public SlettDokumentRestController(SlettDokumentService slettDokumentService,
									   AbacSecurityService abacSecurityService) {
		this.slettDokumentService = slettDokumentService;
		this.abacSecurityService = abacSecurityService;
	}


	@Transactional
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@DeleteMapping("/{journalpostId}/{dokumentInfoId}")
	@ResponseBody
	public SlettDokumentResponse deleteDocumentWithJournalpostIdAndDokumentInfoId(@PathVariable("journalpostId") Long journalpostId, @PathVariable("dokumentInfoId") Long dokumentInfoId) {
		log.info(REQUEST_ID + " har mottat kall med journalpostId=" + journalpostId + " og dokumentInfoId=" + dokumentInfoId);
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
		Utils.validateJournalpostIdAndDokumentId(journalpostId.toString(), dokumentInfoId.toString());
//		abacSecurityService.assertAccessToJournalpost(journalpostId.toString());

		return slettDokumentService.slettDokument(SlettDokumentRequestTo.builder()
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.build());
	}
}