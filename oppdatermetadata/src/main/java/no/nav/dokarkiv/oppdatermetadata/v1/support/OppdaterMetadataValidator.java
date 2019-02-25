package no.nav.dokarkiv.oppdatermetadata.v1.support;

import no.nav.dok.oppdatermetadata.api.v1.PutOppdatermetadataRequest;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;

import java.util.Arrays;

public class OppdaterMetadataValidator {

	public static void validateOppdaterteFelt(PutOppdatermetadataRequest request, JournalStatusCode journalpoststatus) throws InputValideringFeiletException {

	    if (JournalStatusCode.J.equals(journalpoststatus)) {
	        checkIfIllegalFieldIsSet(request.getBruker(), "Bruker", journalpoststatus);
	        checkIfIllegalFieldIsSet(request.getArkivsak(), "Arkivsak", journalpoststatus);
	        checkIfIllegalFieldIsSet(request.getTema(), "Tema", journalpoststatus);
        }

	    else if (Arrays.asList(JournalStatusCode.FS, JournalStatusCode.FL, JournalStatusCode.E).contains(journalpoststatus)) {
            checkIfIllegalFieldIsSet(request.getBruker(), "Bruker", journalpoststatus);
            checkIfIllegalFieldIsSet(request.getArkivsak(), "Arkivsak", journalpoststatus);
            checkIfIllegalFieldIsSet(request.getTema(), "Tema", journalpoststatus);
            checkIfIllegalFieldIsSet(request.getAvsenderMottaker(), "AvsenderMottaker", journalpoststatus);
        }
	}

	private static void checkIfIllegalFieldIsSet(Object field, String fieldName, JournalStatusCode journalpoststatus) throws InputValideringFeiletException {
	    if (field != null) {
	        throw new InputValideringFeiletException(String.format("%s kan ikke oppdateres for journalpost med journalpoststatus %s.", fieldName, journalpoststatus.name()));
        }
    }
}
