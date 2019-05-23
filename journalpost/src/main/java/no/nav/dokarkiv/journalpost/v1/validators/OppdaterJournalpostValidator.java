package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.DokumentInfo;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;

import java.util.Arrays;
import java.util.List;

public final class OppdaterJournalpostValidator {

	private OppdaterJournalpostValidator() {}

	private static List<JournalStatusCode> restrictedJournalpostStatusCodes = Arrays.asList(JournalStatusCode.J, JournalStatusCode.FS, JournalStatusCode.FL, JournalStatusCode.E);

	public static void validateOppdaterteFelt(OppdaterJournalpostRequest request, JournalStatusCode journalpoststatus, JournalpostTypeCode journalpostType) {

		if (restrictedJournalpostStatusCodes.contains(journalpoststatus)) {
            checkIfIllegalFieldIsSet(request.getBruker(), "Bruker", journalpoststatus);
            checkIfIllegalFieldIsSet(request.getSak(), "Sak", journalpoststatus);
			checkIfIllegalFieldIsSet(request.getJournalfoerendeEnhet(), "JournalfoerendeEnhet", journalpoststatus);
			checkIfIllegalFieldIsSet(request.getTema(), "Tema", journalpoststatus);

			if (journalpoststatus != JournalStatusCode.J) {
				validateAvsenderMottaker(request, journalpoststatus);
				validateDokumenter(request, journalpoststatus);
			}
		}

		if (journalpostType != JournalpostTypeCode.U || !restrictedJournalpostStatusCodes.contains(journalpoststatus)) {
			checkIfIllegalFieldIsSet(request.getDatoRetur(), "DatoRetur", journalpoststatus);
		}
	}

	private static void validateAvsenderMottaker(OppdaterJournalpostRequest request, JournalStatusCode journalpoststatus) {
		if (request.getAvsenderMottaker() != null) {
			checkIfIllegalFieldIsSet(request.getAvsenderMottaker().getId(), "AvsenderMottaker.Id", journalpoststatus);
			checkIfIllegalFieldIsSet(request.getAvsenderMottaker().getNavn(), "AvsenderMottaker.Navn", journalpoststatus);
		}
	}

	private static void validateDokumenter(OppdaterJournalpostRequest request, JournalStatusCode journalpoststatus) {
		if (request.getDokumenter() != null) {
			for (DokumentInfo dokument : request.getDokumenter()) {
				checkIfIllegalFieldIsSet(dokument.getBrevkode(), "Brevkode", journalpoststatus);
			}
		}
	}

	private static void checkIfIllegalFieldIsSet(Object field, String fieldName, JournalStatusCode journalpoststatus) {
	    if (field != null) {
	        throw new InputValideringFeiletException(String.format("%s kan ikke oppdateres for journalpost med journalpoststatus %s.", fieldName, journalpoststatus.name()));
        }
    }
}
