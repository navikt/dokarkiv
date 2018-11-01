package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentValidator;
import no.nav.dokarkiv.logiskslettdokument.exceptions.DokumentIkkeSlettetException;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentRequestTo;
import org.slf4j.MDC;
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
			throw new DokumentIkkeSlettetException(String.format(MDC.get(MDCConstants.MDC_REQUEST_ID) + " kan ikke angre logisk sletting av dokument med dokumentInfoId=%s. " +
					"Dokumentet er ikke logisk slettet", dokumentInfo.getDokumentInfoId()));
		}
	}
}
