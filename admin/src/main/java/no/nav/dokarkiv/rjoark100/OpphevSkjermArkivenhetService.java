package no.nav.dokarkiv.rjoark100;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_SKJERMING_TYPE_VARIANT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.SkjermingIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpphevSkjermArkivenhetService {

	private final SkjermingService skjermingService;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	public OpphevSkjermArkivenhetService(
			SkjermingService skjermingService, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository) {
		this.skjermingService = skjermingService;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
	}

	public List<ArkivElementEndringTO> opphevSkjermJournalpost(Long journalpostId, SkjermingTypeCode skjerming) {
		sjekkAtJournalpostErSkjermet(journalpostId, skjerming);
		skjermingService.opphevSkjermJournalpostByJournalpostId(journalpostId);
		return Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement(JOURNALPOST_SKJERMING_TYPE)
						.fraVerdi(skjerming.name())
						.tilVerdi(null)
						.build()
		);
	}

	private List<ArkivElementEndringTO> opphevSkjermingJournalpostDokumentInfoRelasjon(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjerming) {
		sjekkAtJournalpostDokumentInfoRelasjonErSkjermet(journalpostId, dokumentInfoId, skjerming);
		skjermingService.opphevSkjermingJournalpostDokumentInfoRelasjon(journalpostId, dokumentInfoId);

		return Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement(RELASJON_SKJERMING_TYPE)
						.fraVerdi(skjerming.name())
						.tilVerdi(null)
						.build()
		);

	}

	public Map<Long, List<ArkivElementEndringTO>> opphevSkjermDokumentInfo(Long dokumentInfoId, SkjermingTypeCode skjerming) {
		Map<Long, List<ArkivElementEndringTO>> aksjonsLoggMap = new HashMap<>();
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);

		journalpostDokumentInfoRelasjonList.forEach(relasjon -> {
			List<ArkivElementEndringTO> arkivElementEndringList = new ArrayList<>();
			Long journalpostId = relasjon.getJournalpost().getJournalpostId();
			arkivElementEndringList.addAll(opphevSkjermingJournalpostDokumentInfoRelasjon(journalpostId, dokumentInfoId, skjerming));

			if (skjermingService.isJournalpostSkjermet(journalpostId)) {
				arkivElementEndringList.addAll(opphevSkjermJournalpost(journalpostId, skjerming));
			}

			aksjonsLoggMap.put(journalpostId, arkivElementEndringList);

		});

		return aksjonsLoggMap;
	}

	public List<ArkivElementEndringTO> opphevSkjermDokumentFil(Long dokumentInfoId, VariantFormatCode variant, SkjermingTypeCode skjerming) {
		sjekkAtVariantFormatErSkjermet(dokumentInfoId, variant, skjerming);
		skjermingService.opphevSkjermFildetaljerByVariant(dokumentInfoId, variant);
		return Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement(FILDETALJER_SKJERMING_TYPE_VARIANT(variant))
						.fraVerdi(skjerming.name())
						.tilVerdi(null)
						.build()
		);
	}


	private void sjekkAtJournalpostErSkjermet(Long journalpostId, SkjermingTypeCode skjermingTypeCode) {
		if (isFalse(skjermingService.isJournalpostSkjermet(journalpostId, skjermingTypeCode))) {
			throw new SkjermingIkkeFunnetException(String.format(
					"Finner ikke forventet skjerming for journalpost med journalpostId=%s.", journalpostId));
		}
	}

	private void sjekkAtJournalpostDokumentInfoRelasjonErSkjermet(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode) {
		if (isFalse(skjermingService.isJournalpostDokumentInfoRelasjonSkjermet(journalpostId, dokumentInfoId, skjermingTypeCode))) {
			throw new SkjermingIkkeFunnetException(String.format(
					"Finner ikke forventet skjerming for journalpostDokumentInfoRelasjon med journalpostId=%s og dokumentInfoId=%s",
					journalpostId,
					dokumentInfoId));
		}
	}

	private void sjekkAtVariantFormatErSkjermet(Long dokumentInfoId, VariantFormatCode variantFormatCode, SkjermingTypeCode skjermingTypeCode) {
		if (isFalse(skjermingService.isVariantSkjermet(dokumentInfoId, variantFormatCode, skjermingTypeCode))) {
			throw new SkjermingIkkeFunnetException(String.format(
					"Finner ikke forventet skjerming for fildetaljer med dokumentInfoId=%s og variantFormat=%s",
					dokumentInfoId,
					variantFormatCode));
		}
	}
}
