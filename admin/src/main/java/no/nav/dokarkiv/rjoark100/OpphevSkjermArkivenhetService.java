package no.nav.dokarkiv.rjoark100;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.SkjermingIkkeFunnetException;
import org.springframework.stereotype.Service;

@Service
public class OpphevSkjermArkivenhetService {

	private final SkjermingService skjermingService;

	public OpphevSkjermArkivenhetService(
			SkjermingService skjermingService) {
		this.skjermingService = skjermingService;
	}

	public void opphevSkjermJournalpost(Long journalpostId, SkjermingTypeCode skjerming) {
		sjekkAtJournalpostErSkjermet(journalpostId, skjerming);
		skjermingService.opphevSkjermJournalpostByJournalpostId(journalpostId);
	}

	private void sjekkAtJournalpostErSkjermet(Long journalpostId, SkjermingTypeCode skjermingTypeCode) {
		if (isFalse(skjermingService.isJournalpostSkjermet(journalpostId, skjermingTypeCode))) {
			throw new SkjermingIkkeFunnetException(String.format(
					"Finner ikke forventet skjerming for journalpost med journalpostId=%s.", journalpostId));
		}
	}

	public void opphevSkjermDokumentInfo(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjerming) {
		sjekkAtJournalpostDokumentInfoRelasjonErSkjermet(journalpostId, dokumentInfoId, skjerming);
		skjermingService.opphevSkjermingJournalpostDokumentInfoRelasjon(journalpostId, dokumentInfoId);
	}

	private void sjekkAtJournalpostDokumentInfoRelasjonErSkjermet(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode) {
		if (isFalse(skjermingService.isJournalpostDokumentInfoRelasjonSkjermet(journalpostId, dokumentInfoId, skjermingTypeCode))) {
			throw new SkjermingIkkeFunnetException(String.format(
					"Finner ikke forventet skjerming for journalpostDokumentInfoRelasjon med journalpostId=%s og dokumentInfoId=%s",
					journalpostId,
					dokumentInfoId));
		}
	}

	public void opphevSkjermDokumentFil(Long dokumentInfoId, VariantFormatCode variant) {
		sjekkAtVariantFormatErSkjermet(dokumentInfoId, variant);
		skjermingService.opphevSkjermFildetaljerByVariant(dokumentInfoId, variant);
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
