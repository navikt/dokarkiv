package no.nav.dokarkiv.journalpost.v1.util.tilknyttvedlegg;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggRequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;

import java.util.List;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createDokumentVedlegg;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createDokumentVedleggList;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createTilknyttVedleggRequest;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TilknyttVedleggRequestValidatorTest {

	private final TilknyttVedleggRequestValidator tilknyttVedleggRequestValidator = new TilknyttVedleggRequestValidator();

	@BeforeEach
	void setUp() {
		MDC.put(MDC_CONSUMER_ID, "consumerId");
	}

	@Test
	public void happyPath() {
		TilknyttVedleggRequest tilknyttVedleggRequest = createTilknyttVedleggRequest();

		tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest);
	}

	@ParameterizedTest
	@ValueSource(ints = {1, 2})
	@NullSource
	public void shouldValidateRekkefoelgeOk(Integer rekkefoelge) {
		TilknyttVedleggRequest tilknyttVedleggRequest = createTilknyttVedleggRequest("testus testesen", List.of(createDokumentVedlegg(1L, "2", rekkefoelge)));

		tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest);
	}

	@Test
	public void shouldThrowExceptionWhenTilknyttetNavnIsMissing() {
		TilknyttVedleggRequest tilknyttVedleggRequest = createTilknyttVedleggRequest("", createDokumentVedleggList());

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest))
				.withMessage("tilknyttetAvNavn må være satt");
	}

	@Test
	public void shouldThrowExceptionWhenKildeJournalpostIdIsMissing() {
		TilknyttVedleggRequest tilknyttVedleggRequest = createTilknyttVedleggRequest("testus testesen", createDokumentVedleggList(null, "20000000"));

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest))
				.withMessage("dokument.kildeJournalpostId må være satt for vedlegg med dokument.dokumentInfoId=20000000");
	}

	@Test
	public void shouldThrowExceptionWhenKildeDokumentInfoIdIsMissing() {
		TilknyttVedleggRequest tilknyttVedleggRequest = createTilknyttVedleggRequest("testus testesen", createDokumentVedleggList(318883708L, ""));

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest))
				.withMessage("dokument.dokumentInfoId må være satt for vedlegg med dokument.kildeJournalpostId=318883708");
	}

	@ParameterizedTest
	@ValueSource(ints = {0, -1})
	public void shouldThrowExceptionWhenRekkefoelgeLessThan1(int rekkefoelge) {
		TilknyttVedleggRequest tilknyttVedleggRequest = createTilknyttVedleggRequest("testus testesen", List.of(createDokumentVedlegg(1L, "2", rekkefoelge)));

		assertThatExceptionOfType(InputValideringFeiletException.class)
				.isThrownBy(() -> tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest))
				.withMessage("dokument.rekkefoelge må være null eller et positivt heltall. Mottatt rekkefoelge=" + rekkefoelge);
	}
}
