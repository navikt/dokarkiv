package no.nav.dokarkiv.journalpost.v1.util.tilknyttvedlegg;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.KanIkkeTilknytteVedleggException;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggValidator;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.codes.InnsynCode.SKJULES_ORGAN_INTERNT;
import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TilknyttVedleggValidatorTest {

	private final TilknyttVedleggValidator validator = new TilknyttVedleggValidator();

	@Test
	void happyPath() {
		Journalpost targetJournalpost = createJournalpost();
		targetJournalpost.setJournalstatus(JournalStatusCode.D);
		targetJournalpost.setJournalposttype(JournalpostTypeCode.U);

		Journalpost sourceJournalpost = createJournalpost();
		sourceJournalpost.setJournalstatus(JournalStatusCode.J);

		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.kassert(false)
				.build();

		validator.validateJournalpostStatus(targetJournalpost);
		validator.validateSourceJournalpost(sourceJournalpost);
		validator.validateSourceDokumentInfo(dokumentInfo);
	}

	@Test
	void shouldThrowExceptionIfJournalpoststatusIsNotUnderProduksjonD() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.J);

		assertThrows(KanIkkeTilknytteVedleggException.class,
				() -> validator.validateJournalpostStatus(journalpost));
	}

	@Test
	void shouldreturnFalseIfOriginJournalpoststatusIsNotValid() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.D);

		assertThat(validator.validateSourceJournalpost(journalpost), is(false));
	}

	@Test
	void shouldreturnFalseIfOriginJournalpoststatusIsNull() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(null);

		assertThat(validator.validateSourceJournalpost(journalpost), is(false));
	}

	@Test
	void shouldReturnFalseIfJournalpostTypeCodeIsNotUtgaande() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalposttype(JournalpostTypeCode.I);

		assertThrows(KanIkkeTilknytteVedleggException.class,
				() -> validator.validateJournalpostStatus(journalpost));
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
				.dokumentstatus(DokumentStatusCode.UNDER_REDIGERING)
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
