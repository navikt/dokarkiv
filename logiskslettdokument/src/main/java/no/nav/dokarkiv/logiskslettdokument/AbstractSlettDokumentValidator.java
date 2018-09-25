package no.nav.dokarkiv.logiskslettdokument;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskslettdokument.exceptions.ForMangeJournalpostDokumentInfoRelasjonerException;
import no.nav.dokarkiv.logiskslettdokument.exceptions.IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException;
import no.nav.dokarkiv.logiskslettdokument.exceptions.JournalpostDokumentInfoRelasjonNotFoundException;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentRequestTo;
import org.slf4j.MDC;

import java.util.List;

public abstract class AbstractSlettDokumentValidator {

	protected void validateJournalpostDokumentInfoRelasjoner(List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner, Long dokumentInfoId)
			throws JournalpostDokumentInfoRelasjonNotFoundException, ForMangeJournalpostDokumentInfoRelasjonerException {
		if (jpDokInfoRelasjoner.isEmpty()) {
			throw new JournalpostDokumentInfoRelasjonNotFoundException(String.format("%s kan ikke finne noen journalpostDokumentInfoRelasjon for dokumentInfoId=%s",
					MDC.get(MDCConstants.MDC_REQUEST_ID), dokumentInfoId));
		} else if (jpDokInfoRelasjoner.size() > 1) {
			throw new ForMangeJournalpostDokumentInfoRelasjonerException(String.format("%s kan ikke slette dokument som har relasjoner med flere journalposter. " +
					"DokumentinfoId=%s har relasjoner med %s journalposter.", MDC.get(MDCConstants.MDC_REQUEST_ID), dokumentInfoId, jpDokInfoRelasjoner
					.size()));
		}
	}

	protected void validateJournalpostIdBelongsToThisJournalpost(Journalpost journalpost, LogiskSlettDokumentRequestTo requestTo)
			throws IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException {
		if (!journalpost.getJournalpostId().equals(requestTo.getJournalpostId())) {
			throw new IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException(String.format("%s finner ingen journalpostDokumentInfoRelasjon mellom journalpostId=%s " +
					"og dokumentInfoId=%s", MDC.get(MDCConstants.MDC_REQUEST_ID), requestTo.getJournalpostId(), requestTo.getDokumentInfoId()));
		}
	}

}
