package no.nav.dokarkiv.journalpost.v1.util.fjernvedlegg;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.KanIkkeSlettetVedleggKnyttetTilJournalpostException;
import no.nav.dokarkiv.journalpost.v1.validators.FjernVedleggTilknyttetJournalpostValidator;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.journalpost.v1.util.TestDataUtils.JOURNALPOST_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestDataUtils.createDokumentInfoWithLikJournalpost;
import static no.nav.dokarkiv.journalpost.v1.util.TestDataUtils.createJournalpostDokumentInfoRelasjonHovedDok;
import static no.nav.dokarkiv.journalpost.v1.util.TestDataUtils.createJournalpostIngaaende;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class FjernVedleggTilknyttetJournalpostValidatorTest {

	private final FjernVedleggTilknyttetJournalpostValidator fjernVedleggTilknyttetJournalpostValidator = new FjernVedleggTilknyttetJournalpostValidator();

	@Test
	public void shouldThrowExceptionIfJournalpostErIkkeUnderArbeidOgUtgaaende() {
		Journalpost journalpost = createJournalpostIngaaende();

		assertThatExceptionOfType(KanIkkeSlettetVedleggKnyttetTilJournalpostException.class)
				.isThrownBy(() -> fjernVedleggTilknyttetJournalpostValidator.validateJournalPostStatusOgType(journalpost))
				.withMessage("Kan ikke slette vedlegg fra journalpost med journalpostId=1234. Journalposten må være utgående (journalposttype=U) og under arbeid (journalstatus=D). Den har journalposttype=I og journalstatus=FL.");
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoOriginalJournalpostIsEqualsWithInputJournalPost() {
		DokumentInfo dokumentInfo = createDokumentInfoWithLikJournalpost();

		assertThatExceptionOfType(KanIkkeSlettetVedleggKnyttetTilJournalpostException.class)
				.isThrownBy(() -> fjernVedleggTilknyttetJournalpostValidator.validateDokumentInfoOriginalJpNotEqualsInputJournalpost(dokumentInfo, JOURNALPOST_ID))
				.withMessageContaining("Kan ikke fjerne vedlegg fra journalpost hvor vedleggets originalJournalpostId er lik mottatt journalpostId=1234");
	}

	@Test
	public void shouldThrowExceptionJournalpostDokumentInfoRelasjonIfIkkeSomVedlegg() {
		JournalpostDokumentInfoRelasjon jpDokRelasjon = createJournalpostDokumentInfoRelasjonHovedDok();

		assertThatExceptionOfType(KanIkkeSlettetVedleggKnyttetTilJournalpostException.class)
				.isThrownBy(() -> fjernVedleggTilknyttetJournalpostValidator.validateJournalpostDokumentInfoRelasjon(jpDokRelasjon))
				.withMessageContaining("DokumentInfo med dokumentId=1234567 er ikke tilknyttet journalpost med journalpostId=1234 som vedlegg og kan dermed ikke fjernes.");
	}

}