package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.fysiskslettdokument.exceptions.DokumentIkkeLogiskSlettetException;
import no.nav.dokarkiv.logiskslettdokument.exceptions.ForMangeJournalpostDokumentInfoRelasjonerException;
import no.nav.dokarkiv.logiskslettdokument.exceptions.IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException;
import no.nav.dokarkiv.logiskslettdokument.exceptions.JournalpostDokumentInfoRelasjonNotFoundException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FysiskSlettDokumentValidator  {

	public void validateFysiskSlettDokument(List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList, FysiskSlettDokumentRequestTo requestTo) {
		validateJournalpostDokumentInfoRelasjoner(journalpostDokumentInfoRelasjonList, requestTo.getDokumentInfoId());
		validateJournalpostIdBelongsToThisJournalpost(journalpostDokumentInfoRelasjonList.get(0).getJournalpost(), requestTo);
		validateDokumentIkkeLogiskSlettet(journalpostDokumentInfoRelasjonList.get(0).getDokumentInfo());
	}

	public void validateDokumentIkkeLogiskSlettet(DokumentInfo dokumentInfo) throws DokumentIkkeLogiskSlettetException {
		if (isFalse(dokumentInfo.getSlettet())) {
			throw new DokumentIkkeLogiskSlettetException(String.format(MDC.get(MDCConstants.MDC_REQUEST_ID) + " kan ikke utføre fysisk sletting av dokument med dokumentInfoId=%s. " +
					"Dokumentet er ikke logisk slettet", dokumentInfo.getDokumentInfoId()));
		}
	}
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

	protected void validateJournalpostIdBelongsToThisJournalpost(Journalpost journalpost, FysiskSlettDokumentRequestTo requestTo)
			throws IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException {
		if (!journalpost.getJournalpostId().equals(requestTo.getJournalpostId())) {
			throw new IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException(String.format("%s finner ingen journalpostDokumentInfoRelasjon mellom journalpostId=%s " +
					"og dokumentInfoId=%s", MDC.get(MDCConstants.MDC_REQUEST_ID), requestTo.getJournalpostId(), requestTo.getDokumentInfoId()));
		}
	}
}
