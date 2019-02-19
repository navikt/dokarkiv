package no.nav.dokarkiv;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;
import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTOMapper;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.exception.UgyldigSlettArkivenhetInputException;
import no.nav.dokarkiv.rjoark102.SlettArkivenhetOrchestrator;
import no.nav.dokarkiv.dto.SlettArkivenhetRequest;
import no.nav.dokarkiv.dto.SlettArkivenhetResponse;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Slf4j
@RestController
@RequestMapping("rest")
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
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark102"}, percentiles = {0.5, 0.95})
	public SlettArkivenhetResponse slettArkivenhet(
			@RequestHeader(value = AKSJONS_LOGG_HEADER) String aksjonsLoggHeaderString,
			@RequestBody SlettArkivenhetRequest slettArkivenhetRequest) throws UgyldigAksjonsLoggException {
		//TODO: Abac security
		MDC.put(MDCConstants.MDC_REQUEST_ID, "rjoark102");
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har mottat kall om sletting av arkivenhet={} med journalpostId={}, dokumentInfoId={} og variant={}",
				slettArkivenhetRequest.getArkivenhet(), slettArkivenhetRequest.getJournalpostId(), slettArkivenhetRequest.getDokumentInfoId(), slettArkivenhetRequest
						.getVariant());
		assertNotNullOrEmpty(slettArkivenhetRequest.getArkivenhet(), "arkivEnhet");
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();

		switch (slettArkivenhetRequest.getArkivenhet()) {
			case JOURNALPOST:
				assertNotNullOrEmpty(slettArkivenhetRequest.getJournalpostId(), "journalpostId");
				arkivElementEndringTOList = slettArkivenhetOrchestrator.slettJournalpost(slettArkivenhetRequest.getJournalpostId());
				break;
			case VEDLEGG:
				assertNotNullOrEmpty(slettArkivenhetRequest.getJournalpostId(), "journalpostId");
				assertNotNullOrEmpty(slettArkivenhetRequest.getDokumentInfoId(), "dokumentInfoId");
				arkivElementEndringTOList = slettArkivenhetOrchestrator.slettVedlegg(slettArkivenhetRequest.getJournalpostId(), slettArkivenhetRequest
						.getDokumentInfoId());
				break;
			case DOKUMENT_FIL:
				assertNotNullOrEmpty(slettArkivenhetRequest.getDokumentInfoId(), "dokumentInfoId");
				assertNotNullOrEmpty(slettArkivenhetRequest.getVariant(), "variant");
				arkivElementEndringTOList = slettArkivenhetOrchestrator.slettDokumentFil(slettArkivenhetRequest.getDokumentInfoId(), slettArkivenhetRequest
						.getVariant());
				break;
		}

		lagreAksjonsLogg(slettArkivenhetRequest.getJournalpostId(), slettArkivenhetRequest.getDokumentInfoId(), aksjonsLoggHeaderString, arkivElementEndringTOList);

		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) + " har slettet arkivenhet={} med journalpostId={}, dokumentInfoId={} og variant={}",
				slettArkivenhetRequest.getArkivenhet(), slettArkivenhetRequest.getJournalpostId(), slettArkivenhetRequest.getDokumentInfoId(), slettArkivenhetRequest
						.getVariant());
		return SlettArkivenhetResponse.builder()
				.dokumentInfoId(slettArkivenhetRequest.getDokumentInfoId())
				.journalpostId(slettArkivenhetRequest.getJournalpostId())
				.build();

	}

	private void lagreAksjonsLogg(Long journalpostId, Long dokumentInfoId, String aksjonsLoggHeaderString, List<ArkivElementEndringTO> arkivElementEndringTOList) throws
			UgyldigAksjonsLoggException {

		AksjonsLoggTO aksjonsLoggTO = aksjonsLoggTOMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString, AksjonsTypeCode.SLETT, journalpostId, dokumentInfoId);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTO, arkivElementEndringTOList);
	}

	private void assertNotNullOrEmpty(Object value, String parameter) {
		if (Objects.isNull(value) || (value instanceof String && isBlank((String) value))) {
			throw new UgyldigSlettArkivenhetInputException(String.format("Validering av input feilet: Input mangler påkrevd parameter \"%s\"", parameter));
		}
	}

}
