package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static no.nav.dokarkiv.logiskslettdokument.LogiskSlettDokumentRestController.REQUEST_ID;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentValidator;
import no.nav.dokarkiv.logiskslettdokument.exceptions.DokumentIkkeSlettetException;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentRequestTo;
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
			throw new DokumentIkkeSlettetException(String.format(REQUEST_ID + " prøver å angre logisk sletting av et dokument " +
					"som ikke er logisk slettet, dokumentInfoId=%s", dokumentInfo.getDokumentInfoId()));
		}
	}
}
