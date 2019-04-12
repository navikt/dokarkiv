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
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		if (skjermingService.isJournalpostSkjermet(journalpostId, skjerming)) {
			skjermingService.opphevSkjermJournalpostByJournalpostId(journalpostId);
			arkivElementEndringTOList.add(
					ArkivElementEndringTO.builder()
							.arkivElement(JOURNALPOST_SKJERMING_TYPE)
							.fraVerdi(skjerming.name())
							.tilVerdi(null)
							.build()
			);
		}

		return arkivElementEndringTOList;
	}

	private List<ArkivElementEndringTO> opphevSkjermingJournalpostDokumentInfoRelasjon(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjerming) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		skjermingService.opphevSkjermingJournalpostDokumentInfoRelasjon(journalpostId, dokumentInfoId);
		if (skjermingService.isJournalpostDokumentInfoRelasjonSkjermet(journalpostId, dokumentInfoId, skjerming)) {
			arkivElementEndringTOList.add(
					ArkivElementEndringTO.builder()
							.arkivElement(RELASJON_SKJERMING_TYPE)
							.fraVerdi(skjerming.name())
							.tilVerdi(null)
							.build()
			);
		}

		return arkivElementEndringTOList;
	}

	public Map<Long, List<ArkivElementEndringTO>> opphevSkjermDokumentInfo(Long dokumentInfoId, SkjermingTypeCode skjerming) {
		Map<Long, List<ArkivElementEndringTO>> aksjonsLoggMap = new HashMap<>();
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);

		journalpostDokumentInfoRelasjonList.forEach(relasjon -> {
			Long journalpostId = relasjon.getJournalpost().getJournalpostId();
			List<ArkivElementEndringTO> arkivElementEndringList = opphevSkjermingJournalpostDokumentInfoRelasjon(journalpostId, dokumentInfoId, skjerming);

			if (skjermingService.isJournalpostSkjermet(journalpostId, skjerming)) {
				arkivElementEndringList.addAll(opphevSkjermJournalpost(journalpostId, skjerming));
			}

			if (isFalse(arkivElementEndringList.isEmpty())) {
				aksjonsLoggMap.put(journalpostId, arkivElementEndringList);
			}

		});

		return aksjonsLoggMap;
	}

	public List<ArkivElementEndringTO> opphevSkjermDokumentFil(Long dokumentInfoId, VariantFormatCode variant, SkjermingTypeCode skjerming) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		if (skjermingService.isVariantSkjermet(dokumentInfoId, variant, skjerming)) {
			skjermingService.opphevSkjermFildetaljerByVariant(dokumentInfoId, variant);
			arkivElementEndringTOList.add(
					ArkivElementEndringTO.builder()
							.arkivElement(FILDETALJER_SKJERMING_TYPE_VARIANT(variant))
							.fraVerdi(skjerming.name())
							.tilVerdi(null)
							.build()
			);
		}
		return arkivElementEndringTOList;
	}
}
