package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.KanIkkeOppdatereDistribusjonsinfoException;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterDistribusjonsinfoRequest;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;
import static org.junit.jupiter.api.Assertions.fail;

public class OppdaterDistribusjonsinfoValidatorTest {

	private static final OppdaterDistribusjonsinfoRequest request = new OppdaterDistribusjonsinfoRequest(true, UtsendingsKanalCode.SDP.name(), null);
	@Test
	public void shouldValidateWhenFeilregistrertNull() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.FS);
		journalpost.getSaksrelasjon().setFeilregistrert(null);
		OppdaterDistribusjonsinfoValidator.validateJournalpostKanSetteStatusEkspedert(journalpost, request);
	}

	@Test
	public void shouldValidateWhenFeilregistrertFalse() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.FS);
		journalpost.getSaksrelasjon().setFeilregistrert(false);
		OppdaterDistribusjonsinfoValidator.validateJournalpostKanSetteStatusEkspedert(journalpost, request);
	}

	@Test
	public void shouldThrowExceptionWhenFeilregistrertTrue() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.FS);
		journalpost.getSaksrelasjon().setFeilregistrert(true);
		try {
			OppdaterDistribusjonsinfoValidator.validateJournalpostKanSetteStatusEkspedert(journalpost, request);
			fail();
		} catch (KanIkkeOppdatereDistribusjonsinfoException e) {

		}
	}
}