package no.nav.dokarkiv.journalpost.v1.util.tilknyttvedlegg;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.KanIkkeTilknytteVedleggException;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggValidator;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.FERDIGSTILT;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.UNDER_REDIGERING;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.SKJULES_ORGAN_INTERNT;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TilknyttVedleggValidatorTest {

	private final TilknyttVedleggValidator validator = new TilknyttVedleggValidator();

	@Test
	void happyPath() {
		Journalpost targetJournalpost = createJournalpost();
		targetJournalpost.setJournalstatus(D);
		targetJournalpost.setJournalposttype(U);

		Journalpost sourceJournalpost = createJournalpost();
		sourceJournalpost.setJournalstatus(J);

		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.dokumentstatus(FERDIGSTILT)
				.kassert(false)
				.build();

		assertThatCode(() -> {
			validator.validateJournalpostStatus(targetJournalpost);
			validator.validateSourceJournalpost(sourceJournalpost);
			validator.validateSourceDokumentInfo(dokumentInfo);
		}).doesNotThrowAnyException();
	}

	@Test
	void shouldThrowExceptionIfInvalidJournalpoststatus() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(J);

		Exception e = assertThrows(KanIkkeTilknytteVedleggException.class, () -> validator.validateJournalpostStatus(journalpost));
		assertTrue(e.getMessage().contains("journalpost må ha journalstatus=D"));
	}

	@Test
	void shouldThrowExceptionIfInvalidJournalpostTypeCode() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(D);
		journalpost.setJournalposttype(I);

		Exception e = assertThrows(KanIkkeTilknytteVedleggException.class, () -> validator.validateJournalpostStatus(journalpost));
		assertTrue(e.getMessage().contains("journalpost må være en av typene [U, N]"));
	}


	@Test
	void shouldReturnFalseIfOriginJournalpoststatusIsNotValid() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(D);

		assertThat(validator.validateSourceJournalpost(journalpost), is(false));
	}

	@Test
	void shouldReturnFalseIfOriginJournalpoststatusIsNull() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(null);

		assertThat(validator.validateSourceJournalpost(journalpost), is(false));
	}


	@Test
	void shouldReturnFalseWhenSourceJournalpostInnsynIsSKJULES_ORGAN_INTERNT() {
		Journalpost journalpost = createJournalpost();
		journalpost.setInnsyn(SKJULES_ORGAN_INTERNT);

		assertThat(validator.validateSourceJournalpost(journalpost), is(false));
	}

	@Test
	void shouldReturnFalseIfDokumentStatusCodeIsNotFerdigstilt() {
		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.dokumentstatus(UNDER_REDIGERING)
				.build();

		assertThat(validator.validateSourceDokumentInfo(dokumentInfo), is(false));
	}

	@Test
	void shouldReturnFalseIfSlettetIsTrue() {
		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.kassert(true)
				.build();

		assertThat(validator.validateSourceDokumentInfo(dokumentInfo), is(false));
	}

}
