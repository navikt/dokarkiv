package no.nav.dokarkiv.journalpost.v1.util.tilknyttvedlegg;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggRequestValidator;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createTilknyttVedleggRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createTilknyttVedleggRequestUtenNavn;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
public class TilknyttVedleggValidatorTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();


	private TilknyttVedleggRequest tilknyttVedleggRequest;
	private TilknyttVedleggRequestValidator tilknyttVedleggRequestValidatorValidator = new TilknyttVedleggRequestValidator();


	@Test
	public void happyPath() {
		tilknyttVedleggRequest = createTilknyttVedleggRequest();

		tilknyttVedleggRequestValidatorValidator.validateRequest(tilknyttVedleggRequest);

	}

	@Test
	public void shouldThrowExceptionIfTilknyttetNavnIsMissing() {
		tilknyttVedleggRequest = createTilknyttVedleggRequestUtenNavn();

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("TilknyttetAvNavn");

		tilknyttVedleggRequestValidatorValidator.validateRequest(tilknyttVedleggRequest);

	}
}
