package no.nav.dokarkiv.journalpost.v1.util.tilknyttvedlegg;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.InvalidNavConsumerIdFunctionalException;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggRequestValidator;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createDokumentVedleggList;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createTilknyttVedleggRequest;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
public class TilknyttVedleggRequestValidatorTest {

	private TilknyttVedleggRequest tilknyttVedleggRequest;
	private final TilknyttVedleggRequestValidator tilknyttVedleggRequestValidator = new TilknyttVedleggRequestValidator();

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

		assertThrows(InputValideringFeiletException.class,
				() -> tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest),
				"TilknyttetAvNavn må være satt");
	}

	@Test
	public void shouldThrowExceptionIfkildeJournalpostIdIsMissing() {
		MDC.put(MDC_CONSUMER_ID, "srvdokarkivproxy");
		tilknyttVedleggRequest = createTilknyttVedleggRequest("testus testesen", createDokumentVedleggList(null, "20000000"));

		assertThrows(InputValideringFeiletException.class,
				() -> tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest),
				"Kilde journalpostId må være satt");
	}

	@Test
	public void shouldThrowExceptionIfkildeDokumentInfoIdIsMissing() {
		MDC.put(MDC_CONSUMER_ID, "srvdokarkivproxy");
		tilknyttVedleggRequest = createTilknyttVedleggRequest("testus testesen", createDokumentVedleggList(318883708L, ""));

		assertThrows(InputValideringFeiletException.class,
				() -> tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest),
				"DokumentInfoId må være satt");
	}

	@Test
	public void shouldThrowExceptionIfconsumerIdIsMissing() {
		MDC.clear();
		tilknyttVedleggRequest = createTilknyttVedleggRequest();

		assertThrows(InvalidNavConsumerIdFunctionalException.class,
				() -> tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest),
				"Nav-Consumer-Id kan ikke være null");
	}
}
