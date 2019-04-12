package no.nav.dokarkiv.rjoark100;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_SKJERMING_TYPE_VARIANT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.DokumentAlleredeSkjermetException;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
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
		sjekkAtJournalpostIkkeErSkjermet(journalpostId, skjerming);
		skjermingService.skjermJournalpost(journalpostId, skjerming);
		return Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement(JOURNALPOST_SKJERMING_TYPE)
						.fraVerdi(null)
						.tilVerdi(skjerming.name())
						.build()
		);
	}

	public List<ArkivElementEndringTO> skjermJournalpostDokumentInfoRelasjon(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjerming) {
		sjekkAtJournalpostDokumentInfoRelasjonIkkeErSkjermet(journalpostId, dokumentInfoId, skjerming);
		skjermingService.skjermJournalpostDokumentInfoRelasjon(journalpostId, dokumentInfoId, skjerming);
		return Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement(RELASJON_SKJERMING_TYPE)
						.fraVerdi(null)
						.tilVerdi(skjerming.name())
						.build()
		);
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
			aksjonsLoggMap.put(journalpostId, arkivElementEndringList);
		});
		return aksjonsLoggMap;
	}

	private void sjekkAtJournalpostIkkeErSkjermet(Long journalpostId, SkjermingTypeCode skjermingTypeCode) {
		if (skjermingService.isJournalpostSkjermet(journalpostId, skjermingTypeCode)) {
			throw new DokumentAlleredeSkjermetException(String.format(
					"Kan ikke utføre skjerming av journalpost med journalpostId=%s. Journalposten er skjermet",
					journalpostId));
		}
	}

	private void sjekkAtJournalpostDokumentInfoRelasjonIkkeErSkjermet(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode) {
		sjekkAtJournalpostIkkeErSkjermet(journalpostId, skjermingTypeCode);
		if (skjermingService.isJournalpostDokumentInfoRelasjonSkjermet(journalpostId, dokumentInfoId, skjermingTypeCode)) {
			throw new DokumentAlleredeSkjermetException(String.format(
					"Kan ikke utføre skjerming av dokument med journalpostId=%s og dokumentInfoId=%s. Dokumentet er skjermet.",
					journalpostId,
					dokumentInfoId));
		}
	}

	public List<ArkivElementEndringTO> skjermDokumentFil(Long dokumentInfoId, VariantFormatCode variant, SkjermingTypeCode skjerming) {
		sjekkAtVariantFormatIkkeErSkjermet(dokumentInfoId, variant, skjerming);
		skjermingService.skjermFildetaljerByVariant(dokumentInfoId, variant, skjerming);
		return Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement(FILDETALJER_SKJERMING_TYPE_VARIANT(variant))
						.fraVerdi(null)
						.tilVerdi(skjerming.name())
						.build()
		);
	}

	private void sjekkAtVariantFormatIkkeErSkjermet(Long dokumentInfoId, VariantFormatCode variantFormatCode, SkjermingTypeCode skjermingTypeCode) {
		if (skjermingService.isVariantSkjermet(dokumentInfoId, variantFormatCode, skjermingTypeCode)) {
			throw new DokumentAlleredeSkjermetException(String.format(
					"Kan ikke utføre skjerming av dokument med dokumentInfoId=%s og variantformat=%s. Varianten av dokumentet er skjermet.",
					dokumentInfoId,
					variantFormatCode));
		}
	}

	private boolean isJournalpostHarFlereJournalpostRelasjoner(Long journalpostId) {
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(journalpostId);
		return journalpostDokumentInfoRelasjonList.stream()
				.filter(relasjon -> Objects.isNull(relasjon.getSkjermingType()))
				.count() > 1;

	}
}
