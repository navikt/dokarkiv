package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.InvalidNavConsumerIdFunctionalException;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVedlegg;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import org.slf4j.MDC;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class TilknyttVedleggRequestValidator {

	public void validateRequest(TilknyttVedleggRequest request) {
		if (isBlank(MDC.get(MDC_CONSUMER_ID))) {
			throw new InvalidNavConsumerIdFunctionalException("Fant ikke consumerId i MDC. Dette skal utledes automatisk fra token. Ta kontakt med #team_dokumentløsninger");
		}

		validateTilknyttetAvNavn(request.getTilknyttetAvNavn());
		if (!request.getDokument().isEmpty()) {
			request.getDokument().forEach(this::validateDokumentVedlegg);
		}
	}

	private void validateTilknyttetAvNavn(String tilknyttetAvNavn) {
		if (isBlank(tilknyttetAvNavn)) {
			throw new InputValideringFeiletException("tilknyttetAvNavn må være satt");
		}
	}

	private void validateDokumentVedlegg(DokumentVedlegg dokumentVedlegg) {
		if (dokumentVedlegg.getKildeJournalpostId() == null) {
			throw new InputValideringFeiletException("dokument.kildeJournalpostId må være satt for vedlegg med dokument.dokumentInfoId=%s".formatted(dokumentVedlegg.getDokumentInfoId()));
		}
		if (isBlank(dokumentVedlegg.getDokumentInfoId())) {
			throw new InputValideringFeiletException("dokument.dokumentInfoId må være satt for vedlegg med dokument.kildeJournalpostId=%s".formatted(dokumentVedlegg.getKildeJournalpostId()));
		}
	}
}
