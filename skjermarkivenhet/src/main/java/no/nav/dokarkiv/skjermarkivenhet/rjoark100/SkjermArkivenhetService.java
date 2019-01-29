package no.nav.dokarkiv.skjermarkivenhet.rjoark100;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.DokumentAlleredeSkjermetException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
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

	public SkjermArkivenhetResponse skjermArkivenhet(SkjermArkivenhetRequestTo requestTo) {

		validerRequest(requestTo);
		ArkivenhetCode arkivenhetCode = requestTo.getArkivenhet();
		Begrensning begrensning = Begrensning.builder().begrensningType(requestTo.getSkjermingType()).build();

		switch (arkivenhetCode) {
			case JOURNALPOST:
				sjekkAtJournalpostIkkeErSkjermet(requestTo.getJournalpostId(), requestTo.getSkjermingType());
				begrensning.setJournalpostId(requestTo.getJournalpostId());
				break;
			case JOURNALPOST_DOKUMENT:
				sjekkAtJournalpostDokumentInfoRelasjonIkkeErSkjermet(
						requestTo.getJournalpostId(),
						requestTo.getDokumentInfoId(),
						requestTo.getSkjermingType());
				begrensning.setJournalpostId(requestTo.getJournalpostId());
				begrensning.setDokumentInfoId(requestTo.getDokumentInfoId());
				break;
			case DOKUMENT_OBJEKT:
				if (requestTo.getVariantFormat() == null) {
					sjekkAtDokumentObjektIkkeErSkjermet(requestTo.getDokumentInfoId(), requestTo.getSkjermingType());
				} else {
					sjekkAtVariantFormatIkkeErSkjermet(requestTo.getDokumentInfoId(), requestTo.getVariantFormat());
				}
				break;
			default:
				throw new UgyldigInputException("Ugyldig request");
		}
		begrensning.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));
		skjermingService.saveBegrensning(begrensning);

		//Mapper
		return SkjermArkivenhetResponse.builder().build();
	}

	private void validerRequest(SkjermArkivenhetRequestTo requestTo) {
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

	private void sjekkAtDokumentObjektIkkeErSkjermet(Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode) {
		if (skjermingService.isDokumentSkjermet(dokumentInfoId, skjermingTypeCode)) {
			throw new DokumentAlleredeSkjermetException(String.format(
					"Kan ikke utføre skjerming av dokument med dokumentInfoId=%s. Dokumentet er skjermet.",
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


}
