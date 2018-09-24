package no.nav.dokarkiv.logiskslettdokument;

import static no.nav.dokarkiv.logiskslettdokument.LogiskSlettDokumentRestController.REQUEST_ID;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskslettdokument.exceptions.ForMangeJournalpostDokumentInfoRelasjonerException;
import no.nav.dokarkiv.logiskslettdokument.exceptions.IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException;
import no.nav.dokarkiv.logiskslettdokument.exceptions.JournalpostDokumentInfoRelasjonNotFoundException;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentRequestTo;

import java.util.List;

public abstract class AbstractSlettDokumentValidator {

	protected void validateJournalpostDokumentInfoRelasjoner(List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner, Long dokumentInfoId)
			throws JournalpostDokumentInfoRelasjonNotFoundException, ForMangeJournalpostDokumentInfoRelasjonerException {
		if (jpDokInfoRelasjoner.isEmpty()) {
			throw new JournalpostDokumentInfoRelasjonNotFoundException(String.format(REQUEST_ID + " kan ikke finne noen journalpostDokumentInfoRelasjon for dokumentInfoId=%s", dokumentInfoId));
		} else if (jpDokInfoRelasjoner.size() > 1) {
			throw new ForMangeJournalpostDokumentInfoRelasjonerException(String.format(REQUEST_ID + " kan ikke slette dokument som har relasjoner med flere journalposter. " +
					"DokumentinfoId=%s har relasjoner med %s journalposter.", dokumentInfoId, jpDokInfoRelasjoner.size()));
		}
	}

	protected void validateJournalpostIdBelongsToThisJournalpost(Journalpost journalpost, LogiskSlettDokumentRequestTo requestTo)
			throws IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException {
		if (!journalpost.getJournalpostId().equals(requestTo.getJournalpostId())) {
			throw new IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException(String.format(REQUEST_ID + " finner ingen journalpostDokumentInfoRelasjon mellom journalpostId=%s " +
					"og dokumentInfoId=%s", requestTo.getJournalpostId(), requestTo.getDokumentInfoId()));
		}
	}

}
