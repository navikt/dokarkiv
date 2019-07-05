package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.InvalidNavConsumerIdFunctionalException;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVedlegg;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
public class TilknyttVedleggRequestValidator {

	public void validateRequest(TilknyttVedleggRequest request, String navConsumerId) {
		if (navConsumerId == null) {
			throw new InvalidNavConsumerIdFunctionalException(String.format("Nav-Consumer-Id kan ikke være null"));
		}
		validateTilknyttetAvNavn(request.getTilknyttetAvNavn());
		if (!request.getDokument().isEmpty()) {
			request.getDokument().forEach(this::validateDokumentListe);
		}
	}

	private void validateTilknyttetAvNavn(String tilknyttetAvNavn) {
		if (isBlank(tilknyttetAvNavn)) {
			throw new InputValideringFeiletException("TilknyttetAvNavn må være satt");
		}
	}

	private void validateDokumentListe(DokumentVedlegg dokumentVedlegg) {
		if ((dokumentVedlegg.getKildeJournalpostId() == null)) {
			throw new InputValideringFeiletException("Kilde journalpostId må være satt");
		}
		if (isBlank(dokumentVedlegg.getDokumentInfoId())) {
			throw new InputValideringFeiletException("DokumentInfoId må være satt");

		}
	}
}
