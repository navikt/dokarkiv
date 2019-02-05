package no.nav.dokarkiv.skjermarkivenhet.rjoark100;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.DokumentAlleredeSkjermetException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.skjermarkivenhet.SkjermArkivenhetHeader;
import org.slf4j.MDC;
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

	public SkjermArkivenhetResponse skjermArkivenhet(SkjermArkivenhetHeader skjermArkivenhetHeader) {

		switch (skjermArkivenhetHeader.getArkivenhet()) {
			case JOURNALPOST:
				sjekkAtJournalpostIkkeErSkjermet(skjermArkivenhetHeader.getJournalpostId(), skjermArkivenhetHeader.getSkjerming());
				skjermingService.skjermJournalpostByJournalpostIdAndSkjermingType(skjermArkivenhetHeader.getJournalpostId(), skjermArkivenhetHeader
						.getSkjerming());
				break;
			case DOKUMENT_INFO:
				sjekkAtJournalpostDokumentInfoRelasjonIkkeErSkjermet(
						skjermArkivenhetHeader.getJournalpostId(),
						skjermArkivenhetHeader.getDokumentInfoId(),
						skjermArkivenhetHeader.getSkjerming());
				skjermingService.skjermJpDokInfoRelByJournalpostIdAndDokumentInfoIdAndSkjermingType(
						skjermArkivenhetHeader.getJournalpostId(), skjermArkivenhetHeader.getDokumentInfoId(), skjermArkivenhetHeader
								.getSkjerming());
				break;
			case DOKUMENT_FIL:
				sjekkAtVariantFormatIkkeErSkjermet(skjermArkivenhetHeader.getDokumentInfoId(), skjermArkivenhetHeader.getVariant());
				skjermingService.skjermVariantByDokumentInfoIdAndVariantFormatAndSkjermingType(
						skjermArkivenhetHeader.getDokumentInfoId(), skjermArkivenhetHeader.getVariant(), skjermArkivenhetHeader.getSkjerming());
				break;
			default:
				throw new UgyldigInputException("Ugyldig arkivenhet i headeren til " + MDC.get(MDCConstants.MDC_REQUEST_ID));
		}

		return SkjermArkivenhetResponse.builder()
				.journalpostId(skjermArkivenhetHeader.getJournalpostId())
				.dokumentInfoId(skjermArkivenhetHeader.getDokumentInfoId())
				.build();
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

	private void sjekkAtVariantFormatIkkeErSkjermet(Long dokumentInfoId, VariantFormatCode variantFormatCode) {
		if (skjermingService.isVariantSkjermet(dokumentInfoId, variantFormatCode)) {
			throw new DokumentAlleredeSkjermetException(String.format(
					"Kan ikke utføre skjerming av dokument med dokumentInfoId=%s og variantformat=%s. Varianten av dokumentet er skjermet.",
					dokumentInfoId,
					variantFormatCode));
		}
	}

	/**
	 public SkjermArkivenhetResponse opphevSkjermArkivenhet(SkjermArkivenhetHeader skjermArkivenhetHeader){
	 switch (skjermArkivenhetHeader.getArkivenhet()) {
	 case JOURNALPOST:
	 sjekkAtJournalpostErSkjermet(skjermArkivenhetHeader.getJournalpostId(), skjermArkivenhetHeader.getSkjerming());
	 skjermingService.opphevSkjermJournalpostByJournalpostId(skjermArkivenhetHeader.getJournalpostId());
	 break;
	 case DOKUMENT_INFO:
	 sjekkAtJournalpostDokumentInfoRelasjonErSkjermet(
	 skjermArkivenhetHeader.getJournalpostId(),
	 skjermArkivenhetHeader.getDokumentInfoId(),
	 skjermArkivenhetHeader.getSkjerming());
	 skjermingService.opphevSkjermJpDokInfoRelByJournalpostIdAndDokumentInfoId(
	 skjermArkivenhetHeader.getJournalpostId(), skjermArkivenhetHeader.getDokumentInfoId());
	 break;
	 case DOKUMENT_FIL:
	 sjekkAtVariantFormatErSkjermet(skjermArkivenhetHeader.getDokumentInfoId(), skjermArkivenhetHeader.getVariant());
	 skjermingService.opphevSkjermVariantByDokumentInfoIdAndVariantFormat(
	 skjermArkivenhetHeader.getDokumentInfoId(), skjermArkivenhetHeader.getVariant());
	 break;
	 default:
	 throw new UgyldigInputException("Ugyldig arkivenhet i headeren til " + MDC.get(MDCConstants.MDC_REQUEST_ID));
	 }

	 return SkjermArkivenhetResponse.builder()
	 .journalpostId(skjermArkivenhetHeader.getJournalpostId())
	 .dokumentInfoId(skjermArkivenhetHeader.getDokumentInfoId())
	 .build();
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

	 private void sjekkAtVariantFormatErSkjermet(Long dokumentInfoId, VariantFormatCode variantFormatCode) {
	 if (isFalse(skjermingService.isVariantSkjermet(dokumentInfoId, variantFormatCode))) {
	 throw new SkjermingIkkeFunnetException(String.format(
	 "Finner ikke forventet skjerming for fildetaljer med dokumentInfoId=%s og variantFormat=%s",
	 dokumentInfoId,
	 variantFormatCode));
	 }
	 }
	 */
}
