package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.joda.time.DateTime;

public class FinnMottatteJournalposterValidator {
	public static void validate(Journalpost journalpost) {
		validateJournalStatus(journalpost.getJournalstatus());
		validateJournalpostType(journalpost.getJournalposttype());
		validateChangeStamp(journalpost.getChangeStamp());
	}

	private static void validateJournalStatus(JournalStatusCode journalStatusCode){
		if(journalStatusCode == null){
			throw new InvalidArgumentException("journalStatusCode kan ikke være null");
		}
		if(journalStatusCode != JournalStatusCode.MO && journalStatusCode != JournalStatusCode.M){
			throw new InvalidArgumentException("journalStatusCode må være MO eller M");
		}
	}
	private static void validateJournalpostType(JournalpostTypeCode journalpostType) {
		if(journalpostType == null){
			throw new InvalidArgumentException("journalpostTypeCode kan ikke være null");
		}
		if(journalpostType != JournalpostTypeCode.I){
			throw new InvalidArgumentException("journalpostTypeCode må være I");
		}
	}

	private static void validateChangeStamp(ChangeStamp changeStamp){
		if(changeStamp == null){
			throw new InvalidArgumentException("ChangeStamp kan ikke være null");
		}
		if(changeStamp.getCreatedDate() == null){
			throw new InvalidArgumentException("changeStamp.createdDate kan ikke være null");
		}
		if(changeStamp.getCreatedDate().after(DateTime.now().minusWeeks(1).toDate())){
			throw new InvalidArgumentException("changeStamp.createdDate må være eldre enn en(1) uke");
		}
	}
}
