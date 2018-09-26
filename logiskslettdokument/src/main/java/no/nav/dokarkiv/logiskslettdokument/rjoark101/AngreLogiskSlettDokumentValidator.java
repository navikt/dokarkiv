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

	public void validateAngreLogiskSlettDokument(List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner,
																			LogiskSlettDokumentRequestTo requestTo) {
		validateJournalpostDokumentInfoRelasjoner(jpDokInfoRelasjoner, requestTo.getDokumentInfoId());
		validateJournalpostIdBelongsToThisJournalpost(jpDokInfoRelasjoner.get(0).getJournalpost(), requestTo);
		validateDokumentErLogiskSlettet(jpDokInfoRelasjoner.get(0).getDokumentInfo());
	}

	public void validateDokumentErLogiskSlettet(DokumentInfo dokumentInfo) throws DokumentIkkeSlettetException {
		if (isFalse(dokumentInfo.getSlettet())) {
			throw new DokumentIkkeSlettetException(String.format(MDC.get(MDCConstants.MDC_REQUEST_ID) + " kan ikke angre logisk sletting av dokument med dokumentInfoId=%s. " +
					"Dokumentet er ikke logisk slettet", dokumentInfo.getDokumentInfoId()));
		}
	}
}
