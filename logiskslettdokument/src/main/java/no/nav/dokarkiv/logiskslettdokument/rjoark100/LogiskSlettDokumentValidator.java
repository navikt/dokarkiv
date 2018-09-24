package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import static no.nav.dokarkiv.logiskslettdokument.LogiskSlettDokumentRestController.REQUEST_ID;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentValidator;
import no.nav.dokarkiv.logiskslettdokument.exceptions.DokumentAlleredeSlettetException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LogiskSlettDokumentValidator extends AbstractSlettDokumentValidator {

	public void validateLogiskSlettDokument(List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner, LogiskSlettDokumentRequestTo requestTo) {
		validateJournalpostDokumentInfoRelasjoner(jpDokInfoRelasjoner, requestTo.getDokumentInfoId());
		validateJournalpostIdBelongsToThisJournalpost(jpDokInfoRelasjoner.get(0).getJournalpost(), requestTo);
		validateDokumentIkkeLogiskSlettet(jpDokInfoRelasjoner.get(0).getDokumentInfo());
	}

	public void validateDokumentIkkeLogiskSlettet(DokumentInfo dokumentInfo) throws DokumentAlleredeSlettetException {
		if (isTrue(dokumentInfo.getSlettet())) {
			throw new DokumentAlleredeSlettetException(String.format(REQUEST_ID + " prøver å utføre logisk sletting av et dokument " +
					"som allerede er logisk slettet, dokumentInfoId=%s", dokumentInfo.getDokumentInfoId()));
		}
	}
}
