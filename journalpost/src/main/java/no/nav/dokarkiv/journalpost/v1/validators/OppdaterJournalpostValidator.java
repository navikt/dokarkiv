package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.DokumentInfo;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;

import java.util.Arrays;
import java.util.List;

public final class OppdaterJournalpostValidator {

	private static List<JournalStatusCode> restrictedJournalpostStatusCodes = Arrays.asList(JournalStatusCode.J, JournalStatusCode.FS, JournalStatusCode.FL, JournalStatusCode.E);

	private OppdaterJournalpostValidator() {}

	public static void validateOppdaterteFelt(OppdaterJournalpostRequest request, JournalStatusCode journalpostStatus, JournalpostTypeCode journalpostType) {

		if (restrictedJournalpostStatusCodes.contains(journalpostStatus)) {
            checkIfIllegalFieldIsSet(request.getBruker(), "Bruker", journalpostStatus, journalpostType);
            checkIfIllegalFieldIsSet(request.getSak(), "Sak", journalpostStatus, journalpostType);
			checkIfIllegalFieldIsSet(request.getJournalfoerendeEnhet(), "JournalfoerendeEnhet", journalpostStatus, journalpostType);
			checkIfIllegalFieldIsSet(request.getTema(), "Tema", journalpostStatus, journalpostType);

			if (journalpostStatus != JournalStatusCode.J) {
				validateAvsenderMottaker(request, journalpostStatus, journalpostType);
				validateDokumenter(request, journalpostStatus, journalpostType);
			}
		}

		if (journalpostType != JournalpostTypeCode.U || !restrictedJournalpostStatusCodes.contains(journalpostStatus)) {
			checkIfIllegalFieldIsSet(request.getDatoRetur(), "DatoRetur", journalpostStatus, journalpostType);
		}
	}

	private static void validateAvsenderMottaker(OppdaterJournalpostRequest request, JournalStatusCode journalpoststatus, JournalpostTypeCode journalpostType) {
		if (request.getAvsenderMottaker() != null) {
			checkIfIllegalFieldIsSet(request.getAvsenderMottaker().getId(), "AvsenderMottaker.id", journalpoststatus, journalpostType);
			checkIfIllegalFieldIsSet(request.getAvsenderMottaker().getNavn(), "AvsenderMottaker.navn", journalpoststatus, journalpostType);
		}
	}

	private static void validateDokumenter(OppdaterJournalpostRequest request, JournalStatusCode journalpoststatus, JournalpostTypeCode journalpostType) {
		if (request.getDokumenter() != null) {
			for (DokumentInfo dokument : request.getDokumenter()) {
				checkIfIllegalFieldIsSet(dokument.getBrevkode(), "Brevkode", journalpoststatus, journalpostType);
			}
		}
	}

	private static void checkIfIllegalFieldIsSet(Object field, String fieldName, JournalStatusCode journalpoststatus, JournalpostTypeCode journalpostType) {
	    if (field != null) {
	        throw new InputValideringFeiletException(String.format("%s kan ikke oppdateres for journalpost med journalpostStatus=%s og journalpostType=%s.", fieldName, journalpoststatus.name(), journalpostType.name()));
        }
    }
}
