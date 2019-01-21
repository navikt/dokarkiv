package no.nav.dokarkiv.fysiskslettdokument;


import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggHeader;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggHeaderMapper;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggHeaderException;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.fysiskslettdokument.rjoark102.FysiskSlettDokumentRequestTo;
import no.nav.dokarkiv.fysiskslettdokument.rjoark102.FysiskSlettDokumentResponse;
import no.nav.dokarkiv.fysiskslettdokument.rjoark102.FysiskSlettDokumentService;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("rest/fysiskslettdokument")
public class FysiskSlettDokumentRestController {

	private final FysiskSlettDokumentService fysiskSlettDokumentService;
	private final AksjonsLoggService aksjonsLoggService;
	private final AksjonsLoggHeaderMapper aksjonsLoggHeaderMapper;

	@Inject
	public FysiskSlettDokumentRestController(FysiskSlettDokumentService fysiskSlettDokumentService, AksjonsLoggService aksjonsLoggService) {
		this.fysiskSlettDokumentService = fysiskSlettDokumentService;
		this.aksjonsLoggService = aksjonsLoggService;
		this.aksjonsLoggHeaderMapper = new AksjonsLoggHeaderMapper();
	}

	@Transactional
	@ResponseBody
	@DeleteMapping("/{journalpostId}/{dokumentInfoId}/{begrensningType}")
	//TODO: MÅ endre fra value = UPDATE_ACTION til DELETE_ACTION. Men joarkadmin har ikke tilgang.
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
			actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark102"}, percentiles = {0.5, 0.95})
	public FysiskSlettDokumentResponse fysiskSlettDokument(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@PathVariable("journalpostId") Long journalpostId,
			@PathVariable("dokumentInfoId") Long dokumentInfoId,
			@PathVariable("begrensningType") SkjermingTypeCode begrensningType) {
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark102");
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall med journalpostId=" + journalpostId + ", dokumentInfoId=" + dokumentInfoId + " og begrensningType=" + begrensningType);
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		List<AksjonsLoggHeader> aksjonsLoggHeader = aksjonsLoggHeaderMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString);
		aksjonsLoggService.validateAndSaveAksjon(aksjonsLoggHeader);

		return fysiskSlettDokumentService.sletteDokumentFysisk(FysiskSlettDokumentRequestTo.builder()
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.begrensningType(begrensningType)
				.build());
	}

}
