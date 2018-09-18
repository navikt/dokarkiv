package no.nav.dokarkiv.slettdokument.service;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.slettdokument.exceptions.DokumentAlleredeSlettetException;
import no.nav.dokarkiv.slettdokument.exceptions.ForMangeJournalpostDokumentInfoRelasjonerException;
import no.nav.dokarkiv.slettdokument.exceptions.IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException;
import no.nav.dokarkiv.slettdokument.exceptions.JournalpostDokumentInfoRelasjontNotFoundException;

import java.util.List;

/**
 * Interface for SlettDokumentValidator. Used for validating request.
 */
public interface SlettDokumentValidator {

	void validateInputRequest(SlettDokumentRequestTo requestTo) throws IllegalArgumentException;

	void validateJournalpostDokumentInfoRelasjoner(List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner, SlettDokumentRequestTo requestTo)
			throws JournalpostDokumentInfoRelasjontNotFoundException, ForMangeJournalpostDokumentInfoRelasjonerException;

	void validateJournalpostIdBelongsToThisJournalpost(Journalpost journalpost, SlettDokumentRequestTo requestTo)
			throws IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException;

	void validateSletteStatusForDokument(DokumentInfo dokumentInfo) throws DokumentAlleredeSlettetException;

}
