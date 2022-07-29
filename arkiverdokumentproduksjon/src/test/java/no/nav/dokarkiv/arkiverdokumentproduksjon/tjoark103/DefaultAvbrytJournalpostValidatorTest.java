package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusOvergangException;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for AvbrytJournalpostValidator
 *
 * @author Stig Strøm
 */
public class DefaultAvbrytJournalpostValidatorTest {

	private static final Long JOURNALPOST_ID = 42L;
	private final AvbrytJournalpostValidator validator = new DefaultAvbrytJournalpostValidator();

	@Test
	public void shouldValidateOkUnderProduksjon() {
		validator.validate(createJournalpost(JournalStatusCode.D, JournalpostTypeCode.U));
	}

	@Test
	public void shouldValidateOkLokalPrint() {
		validator.validate(createJournalpost(JournalStatusCode.FL, JournalpostTypeCode.U));
	}

	@Test
	public void shouldThrowException_journalpostIsOfIncomingType() {
		assertThrows(UgyldigJournalStatusOvergangException.class,
				() -> validator.validate(createJournalpost(JournalStatusCode.D, JournalpostTypeCode.I)),
				"Kan ikke avbryte en inngående journalpost");
	}

	@Test
	public void shouldThrowException_journalpostIsAlreadyInterrupted() {
		assertThrows(UgyldigJournalStatusOvergangException.class,
				() -> validator.validate(createJournalpost(JournalStatusCode.A, JournalpostTypeCode.U)),
				"Journalpost er allerede avbrutt");
	}

	@Test
	public void shouldThrowException_journalpostIsNotInUnderArbeidStatus() {
		assertThrows(UgyldigJournalStatusOvergangException.class,
				() -> validator.validate(createJournalpost(JournalStatusCode.FS, JournalpostTypeCode.U)),
				"JournalStatus er ikke under arbeid eller lokal print," +
						" journalposten kan derfor ikke avbrytes");
	}

	private Journalpost createJournalpost(JournalStatusCode journalStatusCode, JournalpostTypeCode journalpostTypeCode) {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.journalpostType(journalpostTypeCode)
				.journalStatus(journalStatusCode).build();
	}

}
