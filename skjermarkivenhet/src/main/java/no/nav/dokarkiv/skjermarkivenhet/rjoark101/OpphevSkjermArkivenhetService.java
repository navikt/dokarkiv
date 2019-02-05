package no.nav.dokarkiv.skjermarkivenhet.rjoark101;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.SkjermingIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.skjermarkivenhet.SkjermArkivenhetHeader;
import no.nav.dokarkiv.skjermarkivenhet.rjoark100.SkjermArkivenhetResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class OpphevSkjermArkivenhetService {

	private final SkjermingService skjermingService;

	public OpphevSkjermArkivenhetService(
			SkjermingService skjermingService) {
		this.skjermingService = skjermingService;
	}

	public SkjermArkivenhetResponse opphevSkjermArkivenhet(SkjermArkivenhetHeader skjermArkivenhetHeader) {
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

}
