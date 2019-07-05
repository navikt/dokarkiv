package no.nav.dokarkiv.journalpost.v1.util.tilknyttvedlegg;

import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createDokumentVedleggList;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createTilknyttVedleggRequest;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.InvalidNavConsumerIdFunctionalException;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggRequestValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
public class TilknyttVedleggRequestValidatorTest {

	public static final String NAV_CONSUMER_ID = "Nav-Consumer-Id";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	private TilknyttVedleggRequest tilknyttVedleggRequest;
	private TilknyttVedleggRequestValidator tilknyttVedleggRequestValidator = new TilknyttVedleggRequestValidator();

	@Test
	public void happyPath() {
		tilknyttVedleggRequest = createTilknyttVedleggRequest();
		tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest, NAV_CONSUMER_ID);
	}

	@Test
	public void shouldThrowExceptionIfTilknyttetNavnIsMissing() {
		tilknyttVedleggRequest = createTilknyttVedleggRequest("", createDokumentVedleggList());

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("TilknyttetAvNavn må være satt");
		tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest, NAV_CONSUMER_ID);
	}

	@Test
	public void shouldThrowExceptionIfkildeJournalpostIdIsMissing() {
		tilknyttVedleggRequest = createTilknyttVedleggRequest("testus testesen", createDokumentVedleggList(null, "20000000"));

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Kilde journalpostId må være satt");
		tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest, NAV_CONSUMER_ID);
	}

	@Test
	public void shouldThrowExceptionIfkildeDokumentInfoIdIsMissing() {
		tilknyttVedleggRequest = createTilknyttVedleggRequest("testus testesen", createDokumentVedleggList(318883708L, ""));

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("DokumentInfoId må være satt");
		tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest, NAV_CONSUMER_ID);
	}

	@Test
	public void shouldThrowExceptionIfconsumerIdIsMissing() {
		tilknyttVedleggRequest = createTilknyttVedleggRequest();

		expectedException.expect(InvalidNavConsumerIdFunctionalException.class);
		expectedException.expectMessage("Nav-Consumer-Id kan ikke være null");
		tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest, null);
	}
}
