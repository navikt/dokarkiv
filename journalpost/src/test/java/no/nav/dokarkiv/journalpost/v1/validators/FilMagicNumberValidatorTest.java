package no.nav.dokarkiv.journalpost.v1.validators;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.JPEG;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDF;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PNG;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilMagicNumberValidatorTest {

	@Test
	public void retrunTrueWhenContainsValidatePDFMagicNumber() throws IOException {
		byte[] pdfFile = classpathToInputStream("pdf/pdf/453644598_skan_im_pdfa.pdf");

		boolean isValidPDF = FilMagicNumberValidator.isFileMagicNumberValid(PDF.name(), pdfFile);
		assertTrue(isValidPDF);
	}

	@Test
	public void returnFalseWhenContainsInvalidPDFMagicNumber() throws IOException {
		byte[] pdfFile = classpathToInputStream("pdf/pdf/2021_01_06_nasjonale_tiltak_feil.pdf");

		boolean isValidPDF = FilMagicNumberValidator.isFileMagicNumberValid(PDF.name(), pdfFile);
		assertFalse(isValidPDF);
	}

	@Test
	public void returnTrueWhenContainsValidJPEGMagicNumber() throws IOException {
		byte[] jpegFile = classpathToInputStream("jpeg/2021_01_06_nasjonale_tiltak_16_9.jpg");

		boolean isValidJPEG = FilMagicNumberValidator.isFileMagicNumberValid(JPEG.name(), jpegFile);
		assertTrue(isValidJPEG);
	}

	@Test
	public void returnFalseWhenContainsInvalidJPEGMagicNumber() throws IOException {
		byte[] jpegFile = classpathToInputStream("jpeg/2021_01_06_nasjonale_tiltak_feil.jpg");

		boolean isValidJPEG = FilMagicNumberValidator.isFileMagicNumberValid(JPEG.name(), jpegFile);
		assertFalse(isValidJPEG);
	}

	@Test
	public void returnTrueWhenContainsValidPNGMagicNumber() throws IOException {
		byte[] pngFile = classpathToInputStream("png/2021_01_06_nasjonale_tiltak.png");

		boolean isValidPNG = FilMagicNumberValidator.isFileMagicNumberValid(PNG.name(), pngFile);
		assertTrue(isValidPNG);
	}

	@Test
	public void returnFalseWhenContainsInvalidPNGMagicNumber() throws IOException {
		byte[] pngFile = classpathToInputStream("png/2021_01_06_nasjonale_tiltak_feil.png");

		boolean isValidPNG = FilMagicNumberValidator.isFileMagicNumberValid(PNG.name(), pngFile);
		assertFalse(isValidPNG);
	}

	private static byte[] classpathToInputStream(String classpathResource) throws IOException {
		return new ClassPathResource(classpathResource).getInputStream().readAllBytes();
	}

}