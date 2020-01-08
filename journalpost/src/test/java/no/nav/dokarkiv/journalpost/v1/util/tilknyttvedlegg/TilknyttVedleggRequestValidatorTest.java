package no.nav.dokarkiv.journalpost.v1.util.tilknyttvedlegg;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createDokumentVedleggList;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createTilknyttVedleggRequest;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.InvalidNavConsumerIdFunctionalException;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggRequestValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.MDC;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
public class TilknyttVedleggRequestValidatorTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	private TilknyttVedleggRequest tilknyttVedleggRequest;
	private TilknyttVedleggRequestValidator tilknyttVedleggRequestValidator = new TilknyttVedleggRequestValidator();

	@Test
	public void happyPath() {
		MDC.put(MDC_CONSUMER_ID, "srvdokarkivproxy");
		tilknyttVedleggRequest = createTilknyttVedleggRequest();
		tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest);
	}

	@Test
	public void shouldThrowExceptionIfTilknyttetNavnIsMissing() {
		MDC.put(MDC_CONSUMER_ID, "srvdokarkivproxy");
		tilknyttVedleggRequest = createTilknyttVedleggRequest("", createDokumentVedleggList());

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("TilknyttetAvNavn må være satt");
		tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest);
	}

	@Test
	public void shouldThrowExceptionIfkildeJournalpostIdIsMissing() {
		MDC.put(MDC_CONSUMER_ID, "srvdokarkivproxy");
		tilknyttVedleggRequest = createTilknyttVedleggRequest("testus testesen", createDokumentVedleggList(null, "20000000"));

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("Kilde journalpostId må være satt");
		tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest);
	}

	@Test
	public void shouldThrowExceptionIfkildeDokumentInfoIdIsMissing() {
		MDC.put(MDC_CONSUMER_ID, "srvdokarkivproxy");
		tilknyttVedleggRequest = createTilknyttVedleggRequest("testus testesen", createDokumentVedleggList(318883708L, ""));

		expectedException.expect(InputValideringFeiletException.class);
		expectedException.expectMessage("DokumentInfoId må være satt");
		tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest);
	}

	@Test
	public void shouldThrowExceptionIfconsumerIdIsMissing() {
		tilknyttVedleggRequest = createTilknyttVedleggRequest();

		expectedException.expect(InvalidNavConsumerIdFunctionalException.class);
		expectedException.expectMessage("Nav-Consumer-Id kan ikke være null");
		tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest);
	}
}
