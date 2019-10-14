package no.nav.dokarkiv.journalpost.v1.util.fjernvedlegg;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.SlettIkkeVedleggTilknyttJournalpostException;
import no.nav.dokarkiv.journalpost.v1.api.FjernVedleggTilknyttJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.util.TestDataUtils;
import no.nav.dokarkiv.journalpost.v1.validators.FjernVedleggTilknyttJournalpostValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static no.nav.dokarkiv.journalpost.v1.util.TestDataUtils.JOURNALPOST_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestDataUtils.JOURNALPOST_ID_O;
import static no.nav.dokarkiv.journalpost.v1.util.TestDataUtils.createJournalpostDokumentInfoRelasjonHovedDok;

/**
 * @author Tsigab Angosom Gebremedhin, NAV.
 */

public class FjernVedleggTilknyttJournalpostValidatorTest {


	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	private FjernVedleggTilknyttJournalpostValidator fjernVedleggTilknyttJournalpostValidator = new FjernVedleggTilknyttJournalpostValidator();


	@Test
	public void shouldThrowExceptionIfJournalPostErIkkeUnderArbeidOgUtgaaende() {
		Journalpost hentJournalpost = TestDataUtils.createJournalpostIngaaende();
		expectedException.expect(SlettIkkeVedleggTilknyttJournalpostException.class);
		expectedException.expectMessage("Kan ikke slette vedlegg med journalpostId=1234, Journalpost må være utgående(U) og under arbeid(D)");
		fjernVedleggTilknyttJournalpostValidator.validateJournalPostStatusOgType(hentJournalpost);
	}


	@Test
	public void shouldThrowExceptionIfDokumentInfoOriginalJournalpostIsEqualsWithInputJournalPost() {
		DokumentInfo hentDokumentInfo = TestDataUtils.createDokumentInfoWithLikJournalpost();
		expectedException.expect(SlettIkkeVedleggTilknyttJournalpostException.class);
		expectedException.expectMessage("JounalpostId er lik med originalJournalpostId og vedlagt kan ikke slettes. med journalpostId=1234");
		fjernVedleggTilknyttJournalpostValidator.validateDokumentInfoOriginalJpNotEqualsInputJournalPost(hentDokumentInfo, JOURNALPOST_ID);

	}


	@Test
	public void shouldThrowExceptionJournalpostDokumentInfoRelasjonIfIkkeSomVedlegg(){
		JournalpostDokumentInfoRelasjon jpDokRelasjon= createJournalpostDokumentInfoRelasjonHovedDok();
		expectedException.expect(SlettIkkeVedleggTilknyttJournalpostException.class);
		expectedException.expectMessage("TilknytteJournalpost er ikke som vedlegg og kan ikke slettes");
		fjernVedleggTilknyttJournalpostValidator.validateJournalpostDokumentInfoRelasjon(jpDokRelasjon);

	}

	@Test
	public void shouldThrowInvalidInputExceptionHvisJournalpostIdOgDokumentInfoIdErUgylidy(){
		String journalpostId=null;
		String dokumentinfoId="523684";
		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("");
		fjernVedleggTilknyttJournalpostValidator.validateInput(journalpostId,dokumentinfoId);
	}

}
