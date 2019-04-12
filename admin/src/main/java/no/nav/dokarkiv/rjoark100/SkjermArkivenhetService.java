package no.nav.dokarkiv.rjoark100;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_SKJERMING_TYPE_VARIANT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class SkjermArkivenhetService {

	private final SkjermingService skjermingService;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Inject
	public SkjermArkivenhetService(
			SkjermingService skjermingService, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository) {
		this.skjermingService = skjermingService;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
	}

	public List<ArkivElementEndringTO> skjermJournalpost(Long journalpostId, SkjermingTypeCode skjerming) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		if (isFalse(skjermingService.isJournalpostSkjermet(journalpostId, skjerming))) {
			skjermingService.skjermJournalpost(journalpostId, skjerming);
			arkivElementEndringTOList.add(
					ArkivElementEndringTO.builder()
							.arkivElement(JOURNALPOST_SKJERMING_TYPE)
							.fraVerdi(null)
							.tilVerdi(skjerming.name())
							.build()
			);
		} else {
			log.warn(String.format("Journalpost %s er allerede skjermet med skjermingType=%s. Gjør ingen endring i databasen.", journalpostId, skjerming));
		}

		return arkivElementEndringTOList;
	}

	public List<ArkivElementEndringTO> skjermJournalpostDokumentInfoRelasjon(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjerming) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		if (isFalse(skjermingService.isJournalpostDokumentInfoRelasjonSkjermet(journalpostId, dokumentInfoId, skjerming))) {
			skjermingService.skjermJournalpostDokumentInfoRelasjon(journalpostId, dokumentInfoId, skjerming);
			arkivElementEndringTOList.add(
					ArkivElementEndringTO.builder()
							.arkivElement(RELASJON_SKJERMING_TYPE)
							.fraVerdi(null)
							.tilVerdi(skjerming.name())
							.build()
			);
		} else {
			log.warn(String.format("JournalpostDokumentInfoRelasjon med journalpostId=%s og dokumentInfoId=%s er allerede skjermet med skjermingType=%s. Gjør ingen endring i databasen.", journalpostId, dokumentInfoId, skjerming));
		}

		return arkivElementEndringTOList;
	}

	public Map<Long, List<ArkivElementEndringTO>> skjermDokumentInfo(Long dokumentInfoId, SkjermingTypeCode skjerming) {
		Map<Long, List<ArkivElementEndringTO>> aksjonsLoggMap = new HashMap<>();
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);
		journalpostDokumentInfoRelasjonList.forEach(relasjon -> {
			List<ArkivElementEndringTO> arkivElementEndringList = new ArrayList<>();

			Long journalpostId = relasjon.getJournalpost().getJournalpostId();
			arkivElementEndringList.addAll(skjermJournalpostDokumentInfoRelasjon(journalpostId, dokumentInfoId, skjerming));

			if (isJournalpostHarFlereJournalpostRelasjoner(journalpostId)) {
				arkivElementEndringList.addAll(skjermJournalpost(journalpostId, skjerming));
			}

			if (isFalse(arkivElementEndringList.isEmpty())) {
				aksjonsLoggMap.put(journalpostId, arkivElementEndringList);
			}
		});

		return aksjonsLoggMap;
	}

	public List<ArkivElementEndringTO> skjermDokumentFil(Long dokumentInfoId, VariantFormatCode variant, SkjermingTypeCode skjerming) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();

		if (isFalse(skjermingService.isVariantSkjermet(dokumentInfoId, variant, skjerming))) {
			skjermingService.skjermFildetaljerByVariant(dokumentInfoId, variant, skjerming);
			arkivElementEndringTOList.add(
					ArkivElementEndringTO.builder()
							.arkivElement(FILDETALJER_SKJERMING_TYPE_VARIANT(variant))
							.fraVerdi(null)
							.tilVerdi(skjerming.name())
							.build()
			);
		} else {
			log.warn(String.format("DokumentFil med dokumentInfoId=%s og variantFormat=%s er allerede skjermet med skjermingType=%s. Gjør ingen endring i databasen.", dokumentInfoId, variant, skjerming));
		}

		return arkivElementEndringTOList;
	}

	private boolean isJournalpostHarFlereJournalpostRelasjoner(Long journalpostId) {
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(journalpostId);
		return journalpostDokumentInfoRelasjonList.stream()
				.filter(relasjon -> Objects.isNull(relasjon.getSkjermingType()))
				.count() > 1;

	}
}
