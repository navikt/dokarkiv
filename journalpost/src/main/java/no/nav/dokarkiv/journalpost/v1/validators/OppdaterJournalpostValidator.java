package no.nav.dokarkiv.journalpost.v1.validators;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNumeric;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Sak;

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
		} else {
			validateSak(request.getSak());
		}

		if (journalpostType != JournalpostTypeCode.U || !restrictedJournalpostStatusCodes.contains(journalpostStatus)) {
			checkIfIllegalFieldIsSet(request.getDatoRetur(), "DatoRetur", journalpostStatus, journalpostType);
		}
	}

	private static void validateSak(Sak sak) {
		if(nonNull(sak)) {
			if(!isNumeric(sak.getArkivsaksnummer())) {
				throw new InputValideringFeiletException("Sak.arkivsaksnummer skal være opprettet i GSAK/PSAK og må være et numerisk heltall.");
			}
		}
	}

	private static void checkIfIllegalFieldIsSet(Object field, String fieldName, JournalStatusCode journalpoststatus, JournalpostTypeCode journalpostType) {
	    if (field != null) {
	        throw new InputValideringFeiletException(String.format("%s kan ikke oppdateres for journalpost med journalpostStatus=%s og journalpostType=%s.", fieldName, journalpoststatus.name(), journalpostType.name()));
        }
    }
}
