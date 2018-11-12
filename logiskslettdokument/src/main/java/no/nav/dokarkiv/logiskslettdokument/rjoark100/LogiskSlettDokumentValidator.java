package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LogiskSlettDokumentValidator extends AbstractSlettDokumentValidator {

	protected void validerAtDokumentSomSkalSlettesLogiskErKnyttetTilKunEnJournalpost(
			List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjonList,
			LogiskSlettDokumentRequestTo requestTo) {
		validerAtKunEnGyldigJpDokInfoRelasjonFinnes(jpDokInfoRelasjonList, requestTo.getDokumentInfoId());
		validerAtJournalpostIdOgDokumentInfoIdFraInputHarEnRelasjon(jpDokInfoRelasjonList.get(0)
				.getJournalpost()
				.getJournalpostId(), requestTo);
		if (TilknyttetJournalpostSomCode.HOVEDDOKUMENT.equals(jpDokInfoRelasjonList.get(0).getTilknyttetJournalpostSom())) {
			validerAtJournalpostIkkeErLogiskSlettet(jpDokInfoRelasjonList.get(0)
					.getJournalpost());
		} else {
			validerAtDokumentIkkeErLogiskSlettet(jpDokInfoRelasjonList.get(0)
					.getJournalpost(), jpDokInfoRelasjonList.get(0).getDokumentInfo());
		}
	}

	protected void validerAtDokumentIkkeErLogiskSlettet(Journalpost journalpost, DokumentInfo dokumentInfo) {
//		if (isTrue(dokumentInfo.isBegrenset(journalpost.getJournalpostId(), BegrensningTypeCode.UTILGJENGELIGGJORT))) {
//			throw new DokumentAlleredeSlettetException(
//					String.format("Kan ikke utføre logisk sletting av dokument med " +
//									"dokumentInfoId=%s. Dokumentet er allerede logisk slettet",
//							dokumentInfo.getDokumentInfoId()));
//		}
	}

	protected void validerAtJournalpostIkkeErLogiskSlettet(Journalpost journalpost) {
//		if (isTrue(journalpost.isBegrenset(BegrensningTypeCode.UTILGJENGELIGGJORT))) {
//			throw new DokumentAlleredeSlettetException(
//					String.format("Kan ikke utføre logisk sletting av journalpost med " +
//									"journalpostId=%s. Journalposten er allerede logisk slettet",
//							journalpost.getJournalpostId()));
//		}
	}

}
