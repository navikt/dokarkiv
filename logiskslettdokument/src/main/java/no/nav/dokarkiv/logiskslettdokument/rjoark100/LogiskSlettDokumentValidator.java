package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentValidator;
import no.nav.dokarkiv.logiskslettdokument.exceptions.DokumentAlleredeSlettetException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LogiskSlettDokumentValidator extends AbstractSlettDokumentValidator {

	public void validerAtDokumentSomSkalSlettesLogiskErKnyttetTilKunEnJournalpost(
			List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjonList,
			LogiskSlettDokumentRequestTo requestTo) {
		validerAtKunEnGyldigJpDokInfoRelasjonFinnes(jpDokInfoRelasjonList, requestTo.getDokumentInfoId());
		validerAtJournalpostIdOgDokumentInfoIdFraInputHarEnRelasjon(jpDokInfoRelasjonList.get(0)
				.getJournalpost()
				.getJournalpostId(), requestTo);
		validerAtDokumentIkkeErLogiskSlettet(jpDokInfoRelasjonList.get(0).getDokumentInfo());
	}

	private void validerAtDokumentIkkeErLogiskSlettet(DokumentInfo dokumentInfo) throws DokumentAlleredeSlettetException {
		if (isTrue(dokumentInfo.getSlettet())) {
			throw new DokumentAlleredeSlettetException(
					String.format(MDC.get(MDCConstants.MDC_REQUEST_ID) + " kan ikke utføre logisk sletting av dokument med " +
									"dokumentInfoId=%s. Dokumentet er allerede logisk slettet",
							dokumentInfo.getDokumentInfoId()));
		}
	}
}
