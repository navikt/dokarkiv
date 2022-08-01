package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.KanIkkeOppdatereDistribusjonsinfoException;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;
import static org.junit.jupiter.api.Assertions.fail;

public class OppdaterDistribusjonsinfoValidatorTest {

	@Test
	public void shouldValidateWhenFeilregistrertNull() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.FS);
		journalpost.getSaksrelasjon().setFeilregistrert(null);
		OppdaterDistribusjonsinfoValidator.validateJournalpost(journalpost);
	}

	@Test
	public void shouldValidateWhenFeilregistrertFalse() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.FS);
		journalpost.getSaksrelasjon().setFeilregistrert(false);
		OppdaterDistribusjonsinfoValidator.validateJournalpost(journalpost);
	}

	@Test
	public void shouldThrowExceptionWhenFeilregistrertTrue() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.FS);
		journalpost.getSaksrelasjon().setFeilregistrert(true);
		try {
			OppdaterDistribusjonsinfoValidator.validateJournalpost(journalpost);
			fail();
		} catch (KanIkkeOppdatereDistribusjonsinfoException e) {

		}
	}
}