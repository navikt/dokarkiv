package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
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
		if (TilknyttetJournalpostSomCode.HOVEDDOKUMENT.equals(jpDokInfoRelasjonList.get(0).getTilknyttetJournalpostSom())) {
			validerAtJournalpostErLogiskSlettet(jpDokInfoRelasjonList.get(0).getJournalpost());
		} else {
			validerAtDokumentErLogiskSlettet(jpDokInfoRelasjonList.get(0)
					.getJournalpost(), jpDokInfoRelasjonList.get(0).getDokumentInfo());

		}
	}

	protected void validerAtJournalpostErLogiskSlettet(Journalpost journalpost) {
//		if (isFalse(journalpost.isBegrenset(BegrensningTypeCode.UTILGJENGELIGGJORT))) {
//			throw new DokumentIkkeLogiskSlettetException(String.format("Kan ikke angre logisk sletting av journalpost med journalpostId=%s. " +
//					"Journalposten er ikke logisk slettet", journalpost.getJournalpostId()));
//		}
	}

	protected void validerAtDokumentErLogiskSlettet(Journalpost journalpost, DokumentInfo dokumentInfo) {
//		if (isFalse(dokumentInfo.isBegrenset(journalpost.getJournalpostId(), BegrensningTypeCode.UTILGJENGELIGGJORT))) {
//			throw new DokumentIkkeLogiskSlettetException(String.format("Kan ikke angre logisk sletting av dokument med dokumentInfoId=%s. " +
//					"Dokumentet er ikke logisk slettet", dokumentInfo.getDokumentInfoId()));
//		}
	}
}
