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

	public JournalpostDokumentInfoRelasjon validateLogiskSlettDokument(List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner, LogiskSlettDokumentRequestTo requestTo) {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = validateJournalpostDokumentInfoRelasjoner(jpDokInfoRelasjoner, requestTo
				.getDokumentInfoId());
		validateJournalpostIdBelongsToThisJournalpost(jpDokInfoRelasjoner.get(0).getJournalpost(), requestTo);
		validateDokumentIkkeLogiskSlettet(jpDokInfoRelasjoner.get(0).getDokumentInfo());
	}


	public void validateJournalpostDokumentInfoRelasjoner(List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner, Long dokumentInfoId)
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
			throw new DokumentAlleredeSlettetException(String.format(REQUEST_ID + " prøver å utføre logisk sletting av et dokument " +
					"som allerede er logisk slettet, dokumentInfoId=%s", dokumentInfo.getDokumentInfoId()));
		}
	}
}
