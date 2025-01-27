package no.nav.dokarkiv.journalpost.v1.validators;


import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.KanIkkeKopiereException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

class KopierJournalpostValidatorTest {

	private final KopierJournalpostValidator kopierJournalpostValidator = new KopierJournalpostValidator();

	@ParameterizedTest
	@EnumSource(value = JournalStatusCode.class, names = {"FS", "FL", "E", "J"})
	void skalValidereJournalpostMedGyldigJournalstatus(JournalStatusCode journalStatusCode) {
		Journalpost journalpost = new Journalpost();
		journalpost.setJournalstatus(journalStatusCode);

		assertDoesNotThrow(() -> kopierJournalpostValidator.validate(journalpost));
	}

	@ParameterizedTest
	@EnumSource(value = JournalStatusCode.class, mode = EXCLUDE, names = {"FS", "FL", "E", "J"})
	void skalKasteKanIkkeKopiereExceptionForUgyldigJournalstatus(JournalStatusCode journalStatusCode) {
		Journalpost journalpost = new Journalpost();
		journalpost.setJournalpostId(1L);
		journalpost.setJournalstatus(journalStatusCode);

		assertThatExceptionOfType(KanIkkeKopiereException.class)
				.isThrownBy(() -> kopierJournalpostValidator.validate(journalpost))
				.withMessage("Kan ikke kopiere journalpost med journalpostId=%s fordi journalpost har ugyldig status=%s".formatted(journalpost.getJournalpostId(), journalStatusCode));
	}

}