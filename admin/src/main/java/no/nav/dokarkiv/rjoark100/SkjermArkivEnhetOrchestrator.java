package no.nav.dokarkiv.rjoark100;

import static org.apache.commons.lang3.StringUtils.isBlank;

import no.nav.dokarkiv.core.aksjonslogg.JournalpostDokumentInfoPair;
import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
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
	private final LagreAksjonsLoggService lagreAksjonsLoggService;

	public SkjermArkivEnhetOrchestrator(EndreSkjermingArkivenhetService endreSkjermingArkivenhetService, LagreAksjonsLoggService lagreAksjonsLoggService) {
		this.endreSkjermingArkivenhetService = endreSkjermingArkivenhetService;
		this.lagreAksjonsLoggService = lagreAksjonsLoggService;
	}

	//rjoark100a
	public void skjermArkivEnhet(SkjermArkivenhetRequest request, String hjemmel, String melding, String utfoertAv) throws UgyldigSkjermArkivenhetRequestException, UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList;
		switch (request.getArkivenhet()) {
			case JOURNALPOST:
				assertNotNullOrEmpty(request.getJournalpostId(), "journalpostId");
				Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> aksjonsLoggMapJP = endreSkjermingArkivenhetService.endreSkjermingJournalpost(request
						.getJournalpostId(), request
						.getSkjerming());
				lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.ENDRE_SKJERMING, aksjonsLoggMapJP, hjemmel, melding, utfoertAv);
				break;
			case DOKUMENT_INFO:
				assertNotNullOrEmpty(request.getDokumentInfoId(), "dokumentInfoId");
				Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> aksjonsLoggMapDokInfo = endreSkjermingArkivenhetService.endreSkjermingDokumentInfo(request
						.getDokumentInfoId(), request
						.getSkjerming());
				lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.ENDRE_SKJERMING, aksjonsLoggMapDokInfo, hjemmel, melding, utfoertAv);
				break;
			case DOKUMENT_FIL:
				assertNotNullOrEmpty(request.getDokumentInfoId(), "dokumentInfoId");
				assertNotNullOrEmpty(request.getVariant(), "variant");
				arkivElementEndringTOList = endreSkjermingArkivenhetService.endreSkjermingDokumentFil(request.getDokumentInfoId(), request
						.getVariant(), request
						.getSkjerming());
				lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.ENDRE_SKJERMING, request.getDokumentInfoId(), hjemmel, melding, utfoertAv, arkivElementEndringTOList);
		}

	}

	//rjoark100b
	public void opphevSkjermArkivEnhet(SkjermArkivenhetRequest request, String hjemmel, String melding, String utfoertAv) throws UgyldigSkjermArkivenhetRequestException, UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList;
		switch (request.getArkivenhet()) {
			case JOURNALPOST:
				assertNotNullOrEmpty(request.getJournalpostId(), "journalpostId");
				Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> aksjonsLoggMapJP = endreSkjermingArkivenhetService.endreSkjermingJournalpost(
						request.getJournalpostId(), null);
				lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.ENDRE_SKJERMING, aksjonsLoggMapJP, hjemmel, melding, utfoertAv);
				break;
			case DOKUMENT_INFO:
				assertNotNullOrEmpty(request.getDokumentInfoId(), "dokumentInfoId");
				Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> aksjonsLoggMapDokInfo = endreSkjermingArkivenhetService.endreSkjermingDokumentInfo(request
						.getDokumentInfoId(), null);
				lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.ENDRE_SKJERMING, aksjonsLoggMapDokInfo, hjemmel, melding, utfoertAv);
				break;
			case DOKUMENT_FIL:
				assertNotNullOrEmpty(request.getDokumentInfoId(), "dokumentInfoId");
				assertNotNullOrEmpty(request.getVariant(), "variant");
				arkivElementEndringTOList = endreSkjermingArkivenhetService.endreSkjermingDokumentFil(request.getDokumentInfoId(), request
						.getVariant(), null);
				lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.ENDRE_SKJERMING, request.getDokumentInfoId(), hjemmel, melding, utfoertAv, arkivElementEndringTOList);
		}

	}

	//Gjenbrukt fra AksjonsLoggService men annen exception, legge metoden et annet sted?
	private void assertNotNullOrEmpty(Object value, String parameter) throws UgyldigSkjermArkivenhetRequestException {
		if (Objects.isNull(value) || (value instanceof String && isBlank((String) value))) {
			throw new UgyldigSkjermArkivenhetRequestException("Validering av input feilet: Kallet mangler påkrevd parameter: " + parameter);
		}
	}
}
