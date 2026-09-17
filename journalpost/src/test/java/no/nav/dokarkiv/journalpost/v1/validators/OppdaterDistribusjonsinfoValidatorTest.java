package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.KanIkkeOppdatereDistribusjonsinfoException;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterDistribusjonsinfoRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.SDP;
import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class OppdaterDistribusjonsinfoValidatorTest {

	private static final OppdaterDistribusjonsinfoRequest request = new OppdaterDistribusjonsinfoRequest(true, SDP.name(), null, null);

	@Test
	public void shouldValidateWhenFeilregistrertNull() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(FS);
		journalpost.getSaksrelasjon().setFeilregistrert(null);

		OppdaterDistribusjonsinfoValidator.validateJournalpostKanSetteStatusEkspedert(journalpost, request);
	}

	@Test
	public void shouldValidateWhenFeilregistrertFalse() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(FS);
		journalpost.getSaksrelasjon().setFeilregistrert(false);

		OppdaterDistribusjonsinfoValidator.validateJournalpostKanSetteStatusEkspedert(journalpost, request);
	}

	@Test
	public void shouldThrowExceptionWhenSettStatusEkspedertAndTilbakestillJournalpostAreTrue() {
		OppdaterDistribusjonsinfoRequest oppdaterDistribusjonsinfoRequest = OppdaterDistribusjonsinfoRequest.builder()
				.settStatusEkspedert(true)
				.tilbakestillJournalpost(true)
				.build();

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> OppdaterDistribusjonsinfoValidator.validateRequest(oppdaterDistribusjonsinfoRequest))
				.withMessage("settStatusEkspedert og tilbakestillJournalpost kan ikke være true samtidig");
	}

	@ParameterizedTest
	@EnumSource(value = JournalStatusCode.class, names = {"FS", "FL", "E"})
	public void shouldValidateWhenJournalstatusIsAllowedForDistribution(JournalStatusCode journalStatusCode) {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(journalStatusCode);

		OppdaterDistribusjonsinfoValidator.validateJournalpostKanSetteStatusEkspedert(journalpost, request);
	}

	@ParameterizedTest
	@EnumSource(value = JournalStatusCode.class, names = {"FS", "FL", "E"}, mode = EnumSource.Mode.EXCLUDE)
	public void shouldThrowExceptionWhenJournalstatusIsNotAllowedForDistribution(JournalStatusCode journalStatusCode) {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(journalStatusCode);

		assertThatExceptionOfType(KanIkkeOppdatereDistribusjonsinfoException.class)
				.isThrownBy(() -> OppdaterDistribusjonsinfoValidator.validateJournalpostKanSetteStatusEkspedert(journalpost, request));
	}
}