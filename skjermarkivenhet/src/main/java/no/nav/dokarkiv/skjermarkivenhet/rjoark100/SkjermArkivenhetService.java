package no.nav.dokarkiv.skjermarkivenhet.rjoark100;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.DokumentAlleredeSkjermetException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class SkjermArkivenhetService {

	private final SkjermingService skjermingService;

	@Inject
	public SkjermArkivenhetService(
			SkjermingService skjermingService) {
		this.skjermingService = skjermingService;
	}

	public SkjermArkivenhetResponse skjermJournalpost(Long journalpostId, SkjermingTypeCode skjerming) {
		sjekkAtJournalpostIkkeErSkjermet(journalpostId, skjerming);
		skjermingService.skjermJournalpostByJournalpostIdAndSkjermingType(journalpostId, skjerming);
		return SkjermArkivenhetResponse.builder().journalpostId(journalpostId).build();
	}

	private void sjekkAtJournalpostIkkeErSkjermet(Long journalpostId, SkjermingTypeCode skjermingTypeCode) {
		if (skjermingService.isJournalpostSkjermet(journalpostId, skjermingTypeCode)) {
			throw new DokumentAlleredeSkjermetException(String.format(
					"Kan ikke utføre skjerming av journalpost med journalpostId=%s. Journalposten er skjermet",
					journalpostId));
		}
	}

	public SkjermArkivenhetResponse skjermDokumentInfo(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjerming) {
		sjekkAtJournalpostDokumentInfoRelasjonIkkeErSkjermet(journalpostId, dokumentInfoId, skjerming);
		skjermingService.skjermJpDokInfoRelByJournalpostIdAndDokumentInfoIdAndSkjermingType(journalpostId, dokumentInfoId, skjerming);
		return SkjermArkivenhetResponse.builder()
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.build();
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

	public SkjermArkivenhetResponse skjermDokumentFil(Long dokumentInfoId, VariantFormatCode variant, SkjermingTypeCode skjerming) {
		sjekkAtVariantFormatIkkeErSkjermet(dokumentInfoId, variant);
		skjermingService.skjermVariantByDokumentInfoIdAndVariantFormatAndSkjermingType(dokumentInfoId, variant, skjerming);
		return SkjermArkivenhetResponse.builder()
				.dokumentInfoId(dokumentInfoId)
				.variant(variant)
				.build();
	}

	private void sjekkAtVariantFormatIkkeErSkjermet(Long dokumentInfoId, VariantFormatCode variantFormatCode) {
		if (skjermingService.isVariantSkjermet(dokumentInfoId, variantFormatCode)) {
			throw new DokumentAlleredeSkjermetException(String.format(
					"Kan ikke utføre skjerming av dokument med dokumentInfoId=%s og variantformat=%s. Varianten av dokumentet er skjermet.",
					dokumentInfoId,
					variantFormatCode));
		}
	}
}
