package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusOvergangException;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test for AvbrytJournalpostValidator
 *
 * @author Stig Strøm
 */
public class DefaultAvbrytJournalpostValidatorTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private static final Long JOURNALPOST_ID = 42L;
	private AvbrytJournalpostValidator validator = new DefaultAvbrytJournalpostValidator();

	@Test
	public void shouldValidateOkUnderProduksjon() throws Exception {
		validator.validate(createJournalpost(JournalStatusCode.D, JournalpostTypeCode.U));
	}

	@Test
	public void shouldValidateOkLokalPrint() throws Exception {
		validator.validate(createJournalpost(JournalStatusCode.FL, JournalpostTypeCode.U));
	}

	@Test
	public void shouldThrowException_journalpostIsOfIncomingType() throws Exception {
		expectedException.expect(UgyldigJournalStatusOvergangException.class);
		expectedException.expectMessage("Kan ikke avbryte en inngående journalpost");
		validator.validate(createJournalpost(JournalStatusCode.D, JournalpostTypeCode.I));

	}

	@Test
	public void shouldThrowException_journalpostIsAlreadyInterrupted() throws Exception {
		expectedException.expect(UgyldigJournalStatusOvergangException.class);
		expectedException.expectMessage("Journalpost er allerede avbrutt");
		validator.validate(createJournalpost(JournalStatusCode.A, JournalpostTypeCode.U));
	}

	@Test
	public void shouldThrowException_journalpostIsNotInUnderArbeidStatus() throws Exception {
		expectedException.expect(UgyldigJournalStatusOvergangException.class);
		expectedException.expectMessage("JournalStatus er ikke under arbeid eller lokal print," +
				" journalposten kan derfor ikke avbrytes");
		validator.validate(createJournalpost(JournalStatusCode.FS, JournalpostTypeCode.U));
	}

	private Journalpost createJournalpost(JournalStatusCode journalStatusCode, JournalpostTypeCode journalpostTypeCode) {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.journalpostType(journalpostTypeCode)
				.journalStatus(journalStatusCode).build();
	}

}
