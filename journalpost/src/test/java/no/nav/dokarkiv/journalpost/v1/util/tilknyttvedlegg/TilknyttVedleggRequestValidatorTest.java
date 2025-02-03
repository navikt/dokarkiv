package no.nav.dokarkiv.journalpost.v1.util.tilknyttvedlegg;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggRequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createDokumentVedleggList;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createTilknyttVedleggRequest;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TilknyttVedleggRequestValidatorTest {

	private TilknyttVedleggRequest tilknyttVedleggRequest;
	private final TilknyttVedleggRequestValidator tilknyttVedleggRequestValidator = new TilknyttVedleggRequestValidator();

	@BeforeEach
	void setUp() {
		MDC.put(MDC_CONSUMER_ID, "consumerId");
	}

	@Test
	public void happyPath() {
		tilknyttVedleggRequest = createTilknyttVedleggRequest();

		tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest);
	}

	@Test
	public void shouldThrowExceptionIfTilknyttetNavnIsMissing() {
		tilknyttVedleggRequest = createTilknyttVedleggRequest("", createDokumentVedleggList());

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest))
				.withMessage("tilknyttetAvNavn må være satt");
	}

	@Test
	public void shouldThrowExceptionIfkildeJournalpostIdIsMissing() {
		tilknyttVedleggRequest = createTilknyttVedleggRequest("testus testesen", createDokumentVedleggList(null, "20000000"));

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest))
				.withMessage("dokument.kildeJournalpostId må være satt for vedlegg med dokument.dokumentInfoId=20000000");
	}

	@Test
	public void shouldThrowExceptionIfkildeDokumentInfoIdIsMissing() {
		tilknyttVedleggRequest = createTilknyttVedleggRequest("testus testesen", createDokumentVedleggList(318883708L, ""));

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest))
				.withMessage("dokument.dokumentInfoId må være satt for vedlegg med dokument.kildeJournalpostId=318883708");
	}
}
