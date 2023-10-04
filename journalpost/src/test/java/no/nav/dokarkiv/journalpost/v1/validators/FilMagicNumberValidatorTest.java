package no.nav.dokarkiv.journalpost.v1.validators;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.JPEG;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDF;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PNG;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilMagicNumberValidatorTest {

	@SneakyThrows
	@Test
	public void retrunTrueWhenContainsValidatePDFMagicNumber() {
		byte[] pdfFile = classpathToInputStream("pdf/pdf/453644598_skan_im_pdfa.pdf");

		boolean isValidPDF = FilMagicNumberValidator.isFileMagicNumberValid(PDF.name(), pdfFile);
		assertTrue(isValidPDF);
	}

	@SneakyThrows
	@Test
	public void returnFalseWhenContainsInvalidPDFMagicNumber() {
		byte[] pdfFile = classpathToInputStream("pdf/pdf/2021_01_06_nasjonale_tiltak_feil.pdf");

		boolean isValidPDF = FilMagicNumberValidator.isFileMagicNumberValid(PDF.name(), pdfFile);
		assertFalse(isValidPDF);
	}

	@SneakyThrows
	@Test
	public void returnTrueWhenContainsValidJPEGMagicNumber() {
		byte[] jpegFile = classpathToInputStream("jpeg/2021_01_06_nasjonale_tiltak_16_9.jpg");

		boolean isValidJPEG = FilMagicNumberValidator.isFileMagicNumberValid(JPEG.name(), jpegFile);
		assertTrue(isValidJPEG);
	}

	@SneakyThrows
	@Test
	public void returnFalseWhenContainsInvalidJPEGMagicNumber() {
		byte[] jpegFile = classpathToInputStream("jpeg/2021_01_06_nasjonale_tiltak_feil.jpg");

		boolean isValidJPEG = FilMagicNumberValidator.isFileMagicNumberValid(JPEG.name(), jpegFile);
		assertFalse(isValidJPEG);
	}

	@SneakyThrows
	@Test
	public void returnTrueWhenContainsValidPNGMagicNumber() {
		byte[] pngFile = classpathToInputStream("png/2021_01_06_nasjonale_tiltak.png");

		boolean isValidPNG = FilMagicNumberValidator.isFileMagicNumberValid(PNG.name(), pngFile);
		assertTrue(isValidPNG);
	}

	@SneakyThrows
	@Test
	public void returnFalseWhenContainsInvalidPNGMagicNumber() {
		byte[] pngFile = classpathToInputStream("png/2021_01_06_nasjonale_tiltak_feil.png");

		boolean isValidPNG = FilMagicNumberValidator.isFileMagicNumberValid(PNG.name(), pngFile);
		assertFalse(isValidPNG);
	}

	private static byte[] classpathToInputStream(String classpathResource) throws IOException {
		return new ClassPathResource(classpathResource).getInputStream().readAllBytes();
	}

}