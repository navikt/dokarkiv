package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import org.joda.time.DateTime;

import java.util.Date;

public class FinnMottateJournalposterValidator {
	public static void validate(Journalpost journalpost) {
		journalpost.verifyMandatoryFields();
		validateJournalStatus(journalpost.getJournalstatus());
		validateJournalpostType(journalpost.getJournalposttype());
		validateCreatedDate(journalpost.getChangeStamp().getCreatedDate());
	}

	private static void validateJournalStatus(JournalStatusCode journalStatusCode){
		if(journalStatusCode != JournalStatusCode.MO && journalStatusCode != JournalStatusCode.M){
			throw new InputValideringFeiletException("journalstatus må være MO eller M");
		}
	}
	private static void validateJournalpostType(JournalpostTypeCode journalpostType) {
		if(journalpostType != JournalpostTypeCode.I){
			throw new InputValideringFeiletException("journalposttype må være I");
		}
	}
	private static void validateCreatedDate(Date createdDate) {
		if(createdDate.after(DateTime.now().minusWeeks(1).toDate())){
			throw new InputValideringFeiletException("changeStamp.createddate må være eldre enn en(1) uke");
		}
	}
}
