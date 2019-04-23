package no.nav.dokarkiv.rjoark100;

import static org.apache.commons.lang3.StringUtils.isBlank;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTOMapper;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.exceptions.UgyldigSkjermArkivenhetRequestException;
import no.nav.dokarkiv.dto.SkjermArkivenhetRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class SkjermArkivEnhetOrchestrator {


	private final EndreSkjermingArkivenhetService endreSkjermingArkivenhetService;
	private final AksjonsLoggService aksjonsLoggService;
	private final AksjonsLoggTOMapper aksjonsLoggTOMapper;

	public SkjermArkivEnhetOrchestrator(EndreSkjermingArkivenhetService endreSkjermingArkivenhetService, AksjonsLoggService aksjonsLoggService) {
		this.endreSkjermingArkivenhetService = endreSkjermingArkivenhetService;
		this.aksjonsLoggService = aksjonsLoggService;
		this.aksjonsLoggTOMapper = new AksjonsLoggTOMapper();
	}

	public void skjermArkivEnhet(SkjermArkivenhetRequest request, String aksjonsLoggHeaderString) throws UgyldigSkjermArkivenhetRequestException, UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList;
		switch (request.getArkivenhet()) {
			case JOURNALPOST:
				assertNotNullOrEmpty(request.getJournalpostId(), "journalpostId");
				arkivElementEndringTOList = endreSkjermingArkivenhetService.endreSkjermingJournalpost(request.getJournalpostId(), request
						.getSkjerming());
				lagreAksjonsLogg(request.getJournalpostId(), request.getDokumentInfoId(), aksjonsLoggHeaderString, arkivElementEndringTOList);
				break;
			case DOKUMENT_INFO:
				assertNotNullOrEmpty(request.getDokumentInfoId(), "dokumentInfoId");
				Map<Long, List<ArkivElementEndringTO>> aksjonsLoggMap = endreSkjermingArkivenhetService.endreSkjermingDokumentInfo(request
						.getDokumentInfoId(), request
						.getSkjerming());
				for (Long journalpostId : aksjonsLoggMap.keySet()) {
					lagreAksjonsLogg(journalpostId, request.getDokumentInfoId(), aksjonsLoggHeaderString, aksjonsLoggMap.get(journalpostId));
				}
				break;
			case DOKUMENT_FIL:
				assertNotNullOrEmpty(request.getDokumentInfoId(), "dokumentInfoId");
				assertNotNullOrEmpty(request.getVariant(), "variant");
				arkivElementEndringTOList = endreSkjermingArkivenhetService.endreSkjermingDokumentFil(request.getDokumentInfoId(), request
						.getVariant(), request
						.getSkjerming());
				lagreAksjonsLogg(request.getJournalpostId(), request.getDokumentInfoId(), aksjonsLoggHeaderString, arkivElementEndringTOList);
		}

	}

	public void opphevSkjermArkivEnhet(SkjermArkivenhetRequest request, String aksjonsLoggHeaderString) throws UgyldigSkjermArkivenhetRequestException, UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList;
		switch (request.getArkivenhet()) {
			case JOURNALPOST:
				assertNotNullOrEmpty(request.getJournalpostId(), "journalpostId");
				arkivElementEndringTOList = endreSkjermingArkivenhetService.endreSkjermingJournalpost(
						request.getJournalpostId(), null);
				lagreAksjonsLogg(request.getJournalpostId(), request.getDokumentInfoId(), aksjonsLoggHeaderString, arkivElementEndringTOList);
				break;
			case DOKUMENT_INFO:
				assertNotNullOrEmpty(request.getDokumentInfoId(), "dokumentInfoId");
				Map<Long, List<ArkivElementEndringTO>> aksjonsLoggMap = endreSkjermingArkivenhetService.endreSkjermingDokumentInfo(request
						.getDokumentInfoId(), null);
				for (Long journalpostId : aksjonsLoggMap.keySet()) {
					lagreAksjonsLogg(journalpostId, request.getDokumentInfoId(), aksjonsLoggHeaderString, aksjonsLoggMap.get(journalpostId));
				}
				break;
			case DOKUMENT_FIL:
				assertNotNullOrEmpty(request.getDokumentInfoId(), "dokumentInfoId");
				assertNotNullOrEmpty(request.getVariant(), "variant");
				arkivElementEndringTOList = endreSkjermingArkivenhetService.endreSkjermingDokumentFil(request.getDokumentInfoId(), request
						.getVariant(), null);
				lagreAksjonsLogg(request.getJournalpostId(), request.getDokumentInfoId(), aksjonsLoggHeaderString, arkivElementEndringTOList);
		}

	}

	private void lagreAksjonsLogg(Long journalpostId, Long dokumentInfoId, String aksjonsLoggHeaderString, List<ArkivElementEndringTO> arkivElementEndringTOList) throws
			UgyldigAksjonsLoggException {

		AksjonsLoggTO aksjonsLoggTO = aksjonsLoggTOMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString, AksjonsTypeCode.ENDRE_SKJERMING, journalpostId, dokumentInfoId);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTO, arkivElementEndringTOList);
	}

	//Gjenbrukt fra AksjonsLoggService men annen exception, legge metoden et annet sted?
	private void assertNotNullOrEmpty(Object value, String parameter) throws UgyldigSkjermArkivenhetRequestException {
		if (Objects.isNull(value) || (value instanceof String && isBlank((String) value))) {
			throw new UgyldigSkjermArkivenhetRequestException("Validering av input feilet: Kallet mangler påkrevd parameter: " + parameter);
		}
	}
}
