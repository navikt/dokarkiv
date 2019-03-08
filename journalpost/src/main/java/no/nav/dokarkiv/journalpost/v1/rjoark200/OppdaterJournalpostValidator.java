package no.nav.dokarkiv.journalpost.v1.rjoark200;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;

import java.util.Arrays;

public class OppdaterJournalpostValidator {

	private OppdaterJournalpostValidator() {}

	public static void validateOppdaterteFelt(OppdaterJournalpostRequest request, JournalStatusCode journalpoststatus) {

	    if (JournalStatusCode.J.equals(journalpoststatus)) {
	        checkIfIllegalFieldIsSet(request.getBruker(), "Bruker", journalpoststatus);
	        checkIfIllegalFieldIsSet(request.getArkivsak(), "Arkivsak", journalpoststatus);
	        checkIfIllegalFieldIsSet(request.getTema(), "Tema", journalpoststatus);
        } else if (Arrays.asList(JournalStatusCode.FS, JournalStatusCode.FL, JournalStatusCode.E).contains(journalpoststatus)) {
            checkIfIllegalFieldIsSet(request.getBruker(), "Bruker", journalpoststatus);
            checkIfIllegalFieldIsSet(request.getArkivsak(), "Arkivsak", journalpoststatus);
            checkIfIllegalFieldIsSet(request.getTema(), "Tema", journalpoststatus);
            checkIfIllegalFieldIsSet(request.getAvsenderMottaker(), "AvsenderMottaker", journalpoststatus);
        }
	}

	private static void checkIfIllegalFieldIsSet(Object field, String fieldName, JournalStatusCode journalpoststatus) {
	    if (field != null) {
	        throw new InputValideringFeiletException(String.format("%s kan ikke oppdateres for journalpost med journalpoststatus %s.", fieldName, journalpoststatus.name()));
        }
    }
}
