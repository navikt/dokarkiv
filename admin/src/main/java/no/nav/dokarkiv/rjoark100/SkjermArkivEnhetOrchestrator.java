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
import org.apache.commons.lang3.tuple.Pair;
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

	//rjoark100a
	public void skjermArkivEnhet(SkjermArkivenhetRequest request, String aksjonsLoggHeaderString) throws UgyldigSkjermArkivenhetRequestException, UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList;
		switch (request.getArkivenhet()) {
			case JOURNALPOST:
				assertNotNullOrEmpty(request.getJournalpostId(), "journalpostId");
				Map<Pair<Long, Long>, List<ArkivElementEndringTO>> aksjonsLoggMapJP = endreSkjermingArkivenhetService.endreSkjermingJournalpost(request
						.getJournalpostId(), request
						.getSkjerming());
				lagreAksjonsLogg(aksjonsLoggMapJP, aksjonsLoggHeaderString);
				break;
			case DOKUMENT_INFO:
				assertNotNullOrEmpty(request.getDokumentInfoId(), "dokumentInfoId");
				Map<Pair<Long, Long>, List<ArkivElementEndringTO>> aksjonsLoggMapDokInfo = endreSkjermingArkivenhetService.endreSkjermingDokumentInfo(request
						.getDokumentInfoId(), request
						.getSkjerming());
				lagreAksjonsLogg(aksjonsLoggMapDokInfo, aksjonsLoggHeaderString);
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

	//rjoark100b
	public void opphevSkjermArkivEnhet(SkjermArkivenhetRequest request, String aksjonsLoggHeaderString) throws UgyldigSkjermArkivenhetRequestException, UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList;
		switch (request.getArkivenhet()) {
			case JOURNALPOST:
				assertNotNullOrEmpty(request.getJournalpostId(), "journalpostId");
				Map<Pair<Long, Long>, List<ArkivElementEndringTO>> aksjonsLoggMapJP = endreSkjermingArkivenhetService.endreSkjermingJournalpost(
						request.getJournalpostId(), null);
				lagreAksjonsLogg(aksjonsLoggMapJP, aksjonsLoggHeaderString);
				break;
			case DOKUMENT_INFO:
				assertNotNullOrEmpty(request.getDokumentInfoId(), "dokumentInfoId");
				Map<Pair<Long, Long>, List<ArkivElementEndringTO>> aksjonsLoggMapDokInfo = endreSkjermingArkivenhetService.endreSkjermingDokumentInfo(request
						.getDokumentInfoId(), null);
				lagreAksjonsLogg(aksjonsLoggMapDokInfo, aksjonsLoggHeaderString);
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

	public void lagreAksjonsLogg(Map<Pair<Long, Long>, List<ArkivElementEndringTO>> aksjonsLoggMap, String aksjonsLoggHeaderString) throws
			UgyldigAksjonsLoggException {

		for (Pair<Long, Long> aksjonsLoggJournalpostDokumentInfo : aksjonsLoggMap.keySet()) {
			lagreAksjonsLogg(aksjonsLoggJournalpostDokumentInfo.getLeft(), aksjonsLoggJournalpostDokumentInfo.getRight(), aksjonsLoggHeaderString, aksjonsLoggMap
					.get(aksjonsLoggJournalpostDokumentInfo));
		}
	}

	//Gjenbrukt fra AksjonsLoggService men annen exception, legge metoden et annet sted?
	private void assertNotNullOrEmpty(Object value, String parameter) throws UgyldigSkjermArkivenhetRequestException {
		if (Objects.isNull(value) || (value instanceof String && isBlank((String) value))) {
			throw new UgyldigSkjermArkivenhetRequestException("Validering av input feilet: Kallet mangler påkrevd parameter: " + parameter);
		}
	}
}
