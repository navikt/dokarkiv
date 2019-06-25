package no.nav.dokarkiv.journalpost.v1.util.tilknyttvedlegg;

import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.KanIkkeTilknytteVedleggException;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
public class TilknyttVedleggValidatorTest {

	private TilknyttVedleggValidator validator = new TilknyttVedleggValidator();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void shouldThrowExceptionIfJournalpoststatusIsNotUnderProduksjonD() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.J);

		expectedException.expect(KanIkkeTilknytteVedleggException.class);
		validator.validateJournalpostStatus(journalpost);
	}

	@Test
	public void shouldreturnFalseIfOriginJournalpoststatusIsNotValid() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.D);

		assertThat(validator.validateOriginJournalpostStatus(journalpost), is(false));
	}

	@Test
	public void shouldReturnFalseIfDokumentStatusCodeIsNotFerdigstilt() {
		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.dokumentstatus(DokumentStatusCode.UNDER_REDIGERING)
				.build();
		assertThat(validator.validateDokumentInfo(dokumentInfo), is(false));
	}

}
