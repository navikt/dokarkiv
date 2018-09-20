package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import static no.nav.dokarkiv.logiskslettdokument.LogiskSlettDokumentRestController.REQUEST_ID;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskslettdokument.exceptions.DokumentAlleredeSlettetException;
import no.nav.dokarkiv.logiskslettdokument.exceptions.ForMangeJournalpostDokumentInfoRelasjonerException;
import no.nav.dokarkiv.logiskslettdokument.exceptions.IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException;
import no.nav.dokarkiv.logiskslettdokument.exceptions.JournalpostDokumentInfoRelasjonNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validator class for SlettDokument
 */
@Component
public class LogiskSlettDokumentValidator {

//	public void validateInputRequest(LogiskSlettDokumentRequestTo requestTo) throws IllegalArgumentException {
//		if (requestTo.getJournalpostId() == null) {
//			throw new IllegalArgumentException(REQUEST_ID + " tillater ikke en journalpostId som er lik null");
//		}
//		if (requestTo.getDokumentInfoId() == null) {
//			throw new IllegalArgumentException(REQUEST_ID + " tillater ikke en dokumentInfoId som er lik null");
//		}
//	}

	public JournalpostDokumentInfoRelasjon validateLogiskSlettDokument(List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner, LogiskSlettDokumentRequestTo requestTo) {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = validateJournalpostDokumentInfoRelasjoner(jpDokInfoRelasjoner, requestTo
				.getDokumentInfoId());
		validateJournalpostIdBelongsToThisJournalpost(journalpostDokumentInfoRelasjon.getJournalpost(), requestTo);
		validateDokumentIkkeLogiskSlettet(journalpostDokumentInfoRelasjon.getDokumentInfo());
		return journalpostDokumentInfoRelasjon;
	}


	public JournalpostDokumentInfoRelasjon validateJournalpostDokumentInfoRelasjoner(List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner, Long dokumentInfoId)
			throws JournalpostDokumentInfoRelasjonNotFoundException, ForMangeJournalpostDokumentInfoRelasjonerException {
		if (jpDokInfoRelasjoner.isEmpty()) {
			throw new JournalpostDokumentInfoRelasjonNotFoundException(String.format(REQUEST_ID + " kan ikke finne noen journalpostDokumentInfoRelasjon for dokumentInfoId=%s", dokumentInfoId));
		} else if (jpDokInfoRelasjoner.size() > 1) {
			throw new ForMangeJournalpostDokumentInfoRelasjonerException(String.format(REQUEST_ID + " kan ikke slette dokument som har relasjoner med flere journalposter. " +
					"DokumentinfoId=%s har relasjoner med %s journalposter.", dokumentInfoId, jpDokInfoRelasjoner.size()));
		} else {
			return jpDokInfoRelasjoner.get(0);
		}
	}


	public void validateJournalpostIdBelongsToThisJournalpost(Journalpost journalpost, LogiskSlettDokumentRequestTo requestTo)
			throws IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException {
		if (!journalpost.getJournalpostId().equals(requestTo.getJournalpostId())) {
			throw new IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException(String.format(REQUEST_ID + " finner ingen journalpostDokumentInfoRelasjon mellom journalpostId=%s " +
					"og dokumentInfoId=%s", requestTo.getJournalpostId(), requestTo.getDokumentInfoId()));

		}
	}


	public void validateDokumentIkkeLogiskSlettet(DokumentInfo dokumentInfo) throws DokumentAlleredeSlettetException {
		if (isTrue(dokumentInfo.getSlettet())) {
			throw new DokumentAlleredeSlettetException(String.format(REQUEST_ID + " har allerede slettet dokumentet med dokumentInfoId=%s", dokumentInfo
					.getDokumentInfoId()));
		}
	}
}
