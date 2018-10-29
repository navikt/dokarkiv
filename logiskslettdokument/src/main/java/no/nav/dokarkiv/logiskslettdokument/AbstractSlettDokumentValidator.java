package no.nav.dokarkiv.logiskslettdokument;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.ForMangeJournalpostDokumentInfoRelasjonerException;
import no.nav.dokarkiv.core.exceptions.IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentRequestTo;
import org.slf4j.MDC;

import java.util.List;

public abstract class AbstractSlettDokumentValidator {

	protected void validerAtKunEnGyldigJpDokInfoRelasjonFinnes(
			List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner,
			Long dokumentInfoId) {
		validerAtJournalpostDokumentInfoRelasjonerFinnes(jpDokInfoRelasjoner, dokumentInfoId);
		validerAtJournalpostDokumentInfoRelasjonKunErKnyttetTilEnJournalPost(jpDokInfoRelasjoner, dokumentInfoId);
	}

	protected void validerAtJournalpostDokumentInfoRelasjonerFinnes(
			List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner,
			Long dokumentInfoId) {
		if (jpDokInfoRelasjoner.isEmpty()) {
			throw new JournalpostDokumentInfoRelasjonIkkeFunnetException(
					String.format("%s kan ikke finne noen journalpostDokumentInfoRelasjon for dokumentInfoId=%s",
							MDC.get(MDCConstants.MDC_REQUEST_ID), dokumentInfoId));
		}
	}

	protected void validerAtJournalpostDokumentInfoRelasjonKunErKnyttetTilEnJournalPost(
			List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner,
			Long dokumentInfoId) {
		if (jpDokInfoRelasjoner.size() > 1) {
			throw new ForMangeJournalpostDokumentInfoRelasjonerException(
					String.format("%s kan ikke slette dokument som har relasjoner med flere journalposter. " +
									"DokumentinfoId=%s har relasjoner med %s journalposter.",
							MDC.get(MDCConstants.MDC_REQUEST_ID),
							dokumentInfoId,
							jpDokInfoRelasjoner.size()));
		}
	}

	protected void validerAtJournalpostIdOgDokumentInfoIdFraInputHarEnRelasjon(
			Long journalpostIdFunnetMedDokumentInfoId,
			LogiskSlettDokumentRequestTo requestTo) {
		if (isFalse(journalpostIdFunnetMedDokumentInfoId.equals(requestTo.getJournalpostId()))) {
			throw new IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException(
					String.format("%s finner ingen journalpostDokumentInfoRelasjon mellom journalpostId=%s og dokumentInfoId=%s",
							MDC.get(MDCConstants.MDC_REQUEST_ID),
							requestTo.getJournalpostId(),
							requestTo.getDokumentInfoId()));
		}
	}
}
