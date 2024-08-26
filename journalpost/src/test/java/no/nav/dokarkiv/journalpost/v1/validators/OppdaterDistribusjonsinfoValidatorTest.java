package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.KanIkkeOppdatereDistribusjonsinfoException;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterDistribusjonsinfoRequest;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.SDP;
import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class OppdaterDistribusjonsinfoValidatorTest {

	private static final OppdaterDistribusjonsinfoRequest request = new OppdaterDistribusjonsinfoRequest(true, SDP.name(), null, null);

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

	@Test
	public void shouldThrowExceptionWhenSettStatusEkspedertAndTilbakestillJournalpostAreTrue() {
		OppdaterDistribusjonsinfoRequest oppdaterDistribusjonsinfoRequest = OppdaterDistribusjonsinfoRequest.builder()
				.settStatusEkspedert(true)
				.tilbakestillJournalpost(true)
				.build();
		Throwable exception = assertThrows(InputValideringFeiletException.class, () ->
				OppdaterDistribusjonsinfoValidator.validateRequest(oppdaterDistribusjonsinfoRequest));
		assertEquals("settStatusEkspedert og tilbakestillJournalpost kan ikke være true samtidig", exception.getMessage());
	}
}