package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.UgyldigMediaTypeException;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.journalpost.v1.controllers.SettBrevdataController.VARIANT_FORMAT_ARKIV;
import static no.nav.dokarkiv.journalpost.v1.controllers.SettBrevdataController.VARIANT_FORMAT_PRODUKSJON;
import static no.nav.dokarkiv.journalpost.v1.validators.SettBrevdataValidator.APPLICATION_RTF;
import static no.nav.dokarkiv.journalpost.v1.validators.SettBrevdataValidator.validateRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

class SettBrevdataValidatorTest {
	private static final byte[] BREVDATA = "Hei".getBytes();

	@Test
	void shouldValidateRequestRtf() {
		validateRequest(APPLICATION_RTF, VARIANT_FORMAT_PRODUKSJON, BREVDATA);
	}

	@Test
	void shouldValidateRequestPdf() {
		validateRequest(APPLICATION_PDF_VALUE, VARIANT_FORMAT_ARKIV, BREVDATA);
	}

	@Test
	void shouldThrowExceptionWhenContentTypeNotPdfOrRtf() {
		UgyldigMediaTypeException ugyldigMediaTypeException = assertThrows(UgyldigMediaTypeException.class,
				() -> validateRequest(APPLICATION_XML_VALUE, VARIANT_FORMAT_PRODUKSJON, BREVDATA));
		assertThat(ugyldigMediaTypeException.getMessage()).contains("Content-Type header må være en av");
	}

	@Test
	void shouldThrowExceptionWhenVariantFormatNotArkivOrProduksjon() {
		InputValideringFeiletException inputValideringFeiletException = assertThrows(InputValideringFeiletException.class,
				() -> validateRequest(APPLICATION_RTF, "ORIGINAL", BREVDATA));
		assertThat(inputValideringFeiletException.getMessage()).contains("variantFormat må være en av");
	}

	@Test
	void shouldThrowExceptionWhenPdfWithProduksjonVariant() {
		InputValideringFeiletException inputValideringFeiletException = assertThrows(InputValideringFeiletException.class,
				() -> validateRequest(APPLICATION_PDF_VALUE, VARIANT_FORMAT_PRODUKSJON, BREVDATA));
		assertThat(inputValideringFeiletException.getMessage()).contains("Hvis contentType er application/pdf så må variantFormat være ARKIV");
	}

	@Test
	void shouldThrowExceptionWhenRtfWithArkivVariant() {
		InputValideringFeiletException inputValideringFeiletException = assertThrows(InputValideringFeiletException.class,
				() -> validateRequest(APPLICATION_RTF, VARIANT_FORMAT_ARKIV, BREVDATA));
		assertThat(inputValideringFeiletException.getMessage()).contains("Hvis contentType er application/rtf så må variantFormat være PRODUKSJON");
	}

	@Test
	void shouldThrowExceptionWhenEmptyByteArray() {
		InputValideringFeiletException inputValideringFeiletException = assertThrows(InputValideringFeiletException.class,
				() -> validateRequest(APPLICATION_RTF, VARIANT_FORMAT_PRODUKSJON, "".getBytes()));
		assertThat(inputValideringFeiletException.getMessage()).contains("brevdata må ha lengde mer enn 0");
	}
}