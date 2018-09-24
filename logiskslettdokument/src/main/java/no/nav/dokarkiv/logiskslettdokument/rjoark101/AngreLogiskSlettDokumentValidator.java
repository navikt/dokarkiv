package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static no.nav.dokarkiv.logiskslettdokument.LogiskSlettDokumentRestController.REQUEST_ID;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskslettdokument.exceptions.DokumentAlleredeSlettetException;
import no.nav.dokarkiv.logiskslettdokument.exceptions.DokumentIkkeSlettetException;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentRequestTo;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentValidator;

import java.util.List;

public class AngreLogiskSlettDokumentValidator extends LogiskSlettDokumentValidator {

	public JournalpostDokumentInfoRelasjon validateAngreLogiskSlettDokument(List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner,
																			LogiskSlettDokumentRequestTo requestTo) {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = validateJournalpostDokumentInfoRelasjoner(jpDokInfoRelasjoner,
				requestTo.getDokumentInfoId());
		validateJournalpostIdBelongsToThisJournalpost(journalpostDokumentInfoRelasjon.getJournalpost(), requestTo);
		validateDokumentErLogiskSlettet(journalpostDokumentInfoRelasjon.getDokumentInfo());
		return journalpostDokumentInfoRelasjon;
	}

	private void validateDokumentErLogiskSlettet(DokumentInfo dokumentInfo) throws DokumentAlleredeSlettetException {
		if (isFalse(dokumentInfo.getSlettet())) {
			throw new DokumentIkkeSlettetException(String.format(REQUEST_ID + " prøver å angre logisk sletting av et dokument " +
					"som ikke er logisk slettet, dokumentInfoId=%s", dokumentInfo.getDokumentInfoId()));
		}
	}
}
