package no.nav.dokarkiv.journalpost.v1.util.fjernvedlegg;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.SlettIkkeVedleggTilknyttJournalpostException;
import no.nav.dokarkiv.journalpost.v1.api.FjernVedleggTilknyttJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.util.TestDataUtils;
import no.nav.dokarkiv.journalpost.v1.validators.SlettVedleggTilknyttJournalpostValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static no.nav.dokarkiv.journalpost.v1.util.TestDataUtils.JOURNALPOST_ID_O;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

public class SlettVedleggTilknyttJournalpostValidatorTest {


	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	private SlettVedleggTilknyttJournalpostValidator slettVedleggTilknyttJournalpostValidator = new SlettVedleggTilknyttJournalpostValidator();
	private FjernVedleggTilknyttJournalpostRequest fjernVedleggTilknyttJournalpostRequest;

	@Test
	public void shouldThrowExceptionIfJournalPostErIkkeUnderArbeidOgUtgaaende() {
		Journalpost hentJournalpost = TestDataUtils.createJournalpostIngaaende();
		expectedException.expect(SlettIkkeVedleggTilknyttJournalpostException.class);
		expectedException.expectMessage("Kan ikke slette vedlegg med journalpostId=1234, Journalpost må være utgående(U) og under arbeid(D)");
		slettVedleggTilknyttJournalpostValidator.validateJournalPostStatusOgType(hentJournalpost);
	}


	@Test
	public void shouldThrowExceptionIfDokumentInfoOriginalJournalpostIsEqualsWithInputJournalPost() {
		DokumentInfo hentDokumentInfo = TestDataUtils.createDokumentInfo();
		expectedException.expect(SlettIkkeVedleggTilknyttJournalpostException.class);
		expectedException.expectMessage("JounalpostId er lik med original journalpost og Kan ikke slette vedlegg. journalpostId=12345");
		slettVedleggTilknyttJournalpostValidator.validateDokumentInfoOriginalJpNotEqualsInputJournalPost(hentDokumentInfo, JOURNALPOST_ID_O);

	}


}
