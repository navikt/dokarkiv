package no.nav.dokarkiv.slettdokument.service;

import static no.nav.dokarkiv.slettdokument.SlettDokumentRestController.REQUEST_ID;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.slettdokument.exceptions.DokumentAlleredeSlettetException;
import no.nav.dokarkiv.slettdokument.exceptions.ForMangeJournalpostDokumentInfoRelasjonerException;
import no.nav.dokarkiv.slettdokument.exceptions.IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException;
import no.nav.dokarkiv.slettdokument.exceptions.JournalpostDokumentInfoRelasjontNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validator class for SlettDokument
 */
@Component
public class DefaultSlettDokumentValidator implements SlettDokumentValidator {

	@Override
	public void validateInputRequest(SlettDokumentRequestTo requestTo) throws IllegalArgumentException {
		if (requestTo.getJournalpostId() == null) {
			throw new IllegalArgumentException(REQUEST_ID + " tillater ikke en journalpostId som er lik null");
		}
		if (requestTo.getDokumentInfoId() == null) {
			throw new IllegalArgumentException(REQUEST_ID + " tillater ikke en dokumentInfoId som er lik null");
		}
	}

	@Override
	public void validateJournalpostDokumentInfoRelasjoner(List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner, SlettDokumentRequestTo requestTo)
			throws JournalpostDokumentInfoRelasjontNotFoundException, ForMangeJournalpostDokumentInfoRelasjonerException {
		if (jpDokInfoRelasjoner.isEmpty()) {
			throw new JournalpostDokumentInfoRelasjontNotFoundException(REQUEST_ID + " kan ikke finne noen journalpostDokumentInfoRelasjon for dokumentInfoId=" + requestTo
					.getDokumentInfoId());
		} else if (jpDokInfoRelasjoner.size() > 1) {
			throw new ForMangeJournalpostDokumentInfoRelasjonerException(REQUEST_ID + " kan ikke slette dokument som har relasjoner med flere journalposter. " +
					"DokumentinfoId=" + requestTo.getDokumentInfoId() + " har relasjoner med " + jpDokInfoRelasjoner.size() + " journalposter.");
		} //else jpDokInfoRelasjoner.size()==1)
	}


	@Override
	public void validateJournalpostIdBelongsToThisJournalpost(Journalpost journalpost, SlettDokumentRequestTo requestTo)
			throws IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException {
		if (!journalpost.getJournalpostId().equals(requestTo.getJournalpostId())) {
			throw new IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException(REQUEST_ID + " finner ingen journalpostDokumentInfoRelasjon mellom journalpostId="
					+ requestTo.getJournalpostId() + " og dokumentInfoId=" + requestTo.getDokumentInfoId());
		}
	}


	@Override
	public void validateSletteStatusForDokument(DokumentInfo dokumentInfo) throws DokumentAlleredeSlettetException {
		if (isTrue(dokumentInfo.getSlettet())) {
			throw new DokumentAlleredeSlettetException(REQUEST_ID + " har allerede slettet dokumentet med dokumentInfoId=" + dokumentInfo
					.getDokumentInfoId());
		}
	}
}
