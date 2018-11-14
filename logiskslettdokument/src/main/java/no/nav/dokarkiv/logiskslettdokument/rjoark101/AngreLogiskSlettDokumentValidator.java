package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeLogiskSlettetException;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentValidator;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentRequestTo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AngreLogiskSlettDokumentValidator extends AbstractSlettDokumentValidator {

	protected void validerAngreLogiskSlettAvEttDokument(
			List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjonList,
			LogiskSlettDokumentRequestTo requestTo) {
		validerAtKunEnGyldigJpDokInfoRelasjonFinnes(jpDokInfoRelasjonList, requestTo.getDokumentInfoId());
		validerAtJournalpostIdOgDokumentInfoIdFraInputHarEnRelasjon(jpDokInfoRelasjonList.get(0)
				.getJournalpost()
				.getJournalpostId(), requestTo);
		validerAtDokumentErLogiskSlettet(jpDokInfoRelasjonList.get(0).getDokumentInfo());
	}

	protected void validerAtDokumentErLogiskSlettet(DokumentInfo dokumentInfo) {
		if (isFalse(dokumentInfo.getSlettet())) {
			throw new DokumentIkkeLogiskSlettetException(
					String.format("Kan ikke angre logisk sletting av dokument med dokumentInfoId=%s, fordi dokumentet ikke er logisk slettet."
							, dokumentInfo.getDokumentInfoId()));
		}
	}
}
