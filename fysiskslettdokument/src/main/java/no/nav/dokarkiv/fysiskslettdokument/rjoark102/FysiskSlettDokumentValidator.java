package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.fysiskslettdokument.exceptions.DokumentErIkkeVedleggException;
import no.nav.dokarkiv.fysiskslettdokument.exceptions.DokumentIkkeLogiskSlettetException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class FysiskSlettDokumentValidator {

	public void validerAtKunEtVedleggSkalSlettes(
			JournalpostDokumentInfoRelasjon jpDokInfoRelSomSkalSlettes,
			FysiskSlettDokumentRequestTo requestTo) {
		validerAtJournalpostDokumentInfoRelasjonFinnes(jpDokInfoRelSomSkalSlettes, requestTo.getJournalpostId());
		validerAtDokumentErLogiskSlettet(jpDokInfoRelSomSkalSlettes.getDokumentInfo(), requestTo);
		validerAtRelasjonErTilknyttetSomVedlegg(jpDokInfoRelSomSkalSlettes, requestTo);
	}

	private void validerAtJournalpostDokumentInfoRelasjonFinnes(
			JournalpostDokumentInfoRelasjon jpDokInfoRelSomSkalSlettes,
			Long journalpostId) {
		if (isNull(jpDokInfoRelSomSkalSlettes)) {
			//TODO: Bruk denne også i logiskslettdokument modulen etter merge av branch visSletteStatusIDokumentInfoTittel
			throw new JournalpostDokumentInfoRelasjonIkkeFunnetException(
					String.format("%s kan ikke finne journalpostDokumentInfoRelasjon med journalpostId=%s",
							MDC.get(MDCConstants.MDC_REQUEST_ID),
							journalpostId));
		}
	}

	private static void validerAtDokumentErLogiskSlettet(
			DokumentInfo dokumentInfo,
			FysiskSlettDokumentRequestTo requestTo) {
		if (isNotTrue(dokumentInfo.getSlettet())) {
			throw new DokumentIkkeLogiskSlettetException(String.format(
					"%s kan ikke fysisk slette dokument som ikke er logisk slettet. dokumenInfoId=%s, journalpostId=%s",
					MDC.get(MDCConstants.MDC_REQUEST_ID),
					requestTo.getDokumentInfoId(),
					requestTo.getJournalpostId()));
		}
	}

	private static void validerAtRelasjonErTilknyttetSomVedlegg(
			JournalpostDokumentInfoRelasjon jpDokInfoRelSomSkalSlettes,
			FysiskSlettDokumentRequestTo requestTo) {
		if (isNotTrue(jpDokInfoRelSomSkalSlettes.isVedlegg())) {
			//TODO: Se på gjennbruk av Exception fra logiskSlettdokument og denne modulen
			throw new DokumentErIkkeVedleggException(
					String.format("%s kan ikke slette dokument som ikke er et vedlegg når hjemmel=%s er brukt. " +
									"dokumentInfoId=%s, journalpostId=%s",
							MDC.get(MDCConstants.MDC_REQUEST_ID),
							requestTo.getHjemmel(),
							requestTo.getDokumentInfoId(),
							requestTo.getJournalpostId()));
		}
	}
}
