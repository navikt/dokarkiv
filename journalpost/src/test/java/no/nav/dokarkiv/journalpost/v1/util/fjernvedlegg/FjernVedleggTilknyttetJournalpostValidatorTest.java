package no.nav.dokarkiv.journalpost.v1.util.fjernvedlegg;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.KanIkkeSlettetVedleggKnyttetTilJournalpostException;
import no.nav.dokarkiv.journalpost.v1.util.TestDataUtils;
import no.nav.dokarkiv.journalpost.v1.validators.FjernVedleggTilknyttetJournalpostValidator;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.journalpost.v1.util.TestDataUtils.JOURNALPOST_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestDataUtils.createJournalpostDokumentInfoRelasjonHovedDok;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */
public class FjernVedleggTilknyttetJournalpostValidatorTest {

	private final FjernVedleggTilknyttetJournalpostValidator fjernVedleggTilknyttetJournalpostValidator = new FjernVedleggTilknyttetJournalpostValidator();

	@Test
	public void shouldThrowExceptionIfJournalPostErIkkeUnderArbeidOgUtgaaende() {
		Journalpost hentJournalpost = TestDataUtils.createJournalpostIngaaende();

		assertThrows(KanIkkeSlettetVedleggKnyttetTilJournalpostException.class,
				() -> fjernVedleggTilknyttetJournalpostValidator.validateJournalPostStatusOgType(hentJournalpost),
				"Kan ikke slette vedlegg med journalpostId=1234, Journalpost må være utgående(U) og under arbeid(D)");
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoOriginalJournalpostIsEqualsWithInputJournalPost() {
		DokumentInfo hentDokumentInfo = TestDataUtils.createDokumentInfoWithLikJournalpost();

		assertThrows(KanIkkeSlettetVedleggKnyttetTilJournalpostException.class,
				() -> fjernVedleggTilknyttetJournalpostValidator.validateDokumentInfoOriginalJpNotEqualsInputJournalpost(hentDokumentInfo, JOURNALPOST_ID),
				"Kan ikke slette vedlegg med journalpostId=1234, Journalpost må være utgående(U) og under arbeid(D)");
	}

	@Test
	public void shouldThrowExceptionJournalpostDokumentInfoRelasjonIfIkkeSomVedlegg() {
		JournalpostDokumentInfoRelasjon jpDokRelasjon = createJournalpostDokumentInfoRelasjonHovedDok();

		assertThrows(KanIkkeSlettetVedleggKnyttetTilJournalpostException.class,
				() -> fjernVedleggTilknyttetJournalpostValidator.validateJournalpostDokumentInfoRelasjon(jpDokRelasjon),
				"TilknytteJournalpost er ikke som vedlegg og kan ikke slettes");
	}

	@Test
	public void shouldThrowInvalidInputExceptionHvisJournalpostIdOgDokumentInfoIdErUgylidy() {
		String journalpostId = null;
		String dokumentinfoId = "523684";

		assertThrows(InputValideringFeiletException.class,
				() -> fjernVedleggTilknyttetJournalpostValidator.validateInput(journalpostId, dokumentinfoId),
				"");
	}

}
