package no.nav.dokarkiv.core.util;

import lombok.SneakyThrows;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
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
		FilDetaljer filDetaljer = createFilDetaljer(pdfFile, PDF);

		boolean isValidPDF = FilMagicNumberValidator.isFileMagicNumberValid(filDetaljer);
		assertTrue(isValidPDF);
	}

	@SneakyThrows
	@Test
	public void returnFalseWhenContainsInvalidPDFMagicNumber() {
		byte[] pdfFile = classpathToInputStream("pdf/pdf/2021_01_06_nasjonale_tiltak_feil.pdf");
		FilDetaljer filDetaljer = createFilDetaljer(pdfFile, PDF);

		boolean isValidPDF = FilMagicNumberValidator.isFileMagicNumberValid(filDetaljer);
		assertFalse(isValidPDF);
	}

	@SneakyThrows
	@Test
	public void returnTrueWhenContainsValidJPEGMagicNumber() {
		byte[] jpegFile = classpathToInputStream("jpeg/2021_01_06_nasjonale_tiltak_16_9.jpg");
		FilDetaljer filDetaljer = createFilDetaljer(jpegFile, JPEG);

		boolean isValidJPEG = FilMagicNumberValidator.isFileMagicNumberValid(filDetaljer);
		assertTrue(isValidJPEG);
	}

	@SneakyThrows
	@Test
	public void returnFalseWhenContainsInvalidJPEGMagicNumber() {
		byte[] jpegFile = classpathToInputStream("jpeg/2021_01_06_nasjonale_tiltak_feil.jpg");
		FilDetaljer filDetaljer = createFilDetaljer(jpegFile, JPEG);

		boolean isValidJPEG = FilMagicNumberValidator.isFileMagicNumberValid(filDetaljer);
		assertFalse(isValidJPEG);
	}

	@SneakyThrows
	@Test
	public void returnTrueWhenContainsValidPNGMagicNumber() {
		byte[] pngFile = classpathToInputStream("png/2021_01_06_nasjonale_tiltak.png");
		FilDetaljer filDetaljer = createFilDetaljer(pngFile, PNG);

		boolean isValidPNG = FilMagicNumberValidator.isFileMagicNumberValid(filDetaljer);
		assertTrue(isValidPNG);
	}

	@SneakyThrows
	@Test
	public void returnFalseWhenContainsInvalidPNGMagicNumber() {
		byte[] jpegFile = classpathToInputStream("png/2021_01_06_nasjonale_tiltak_feil.png");
		FilDetaljer filDetaljer = createFilDetaljer(jpegFile, PNG);

		boolean isValidPNG = FilMagicNumberValidator.isFileMagicNumberValid(filDetaljer);
		assertFalse(isValidPNG);
	}

	private FilDetaljer createFilDetaljer(byte[] fil, FilTypeCode filTypeCode) {
		FilDetaljer detaljer = new FilDetaljer();
		detaljer.setFileContent(fil);
		detaljer.setFiltype(filTypeCode);
		return detaljer;
	}

	private static byte[] classpathToInputStream(String classpathResource) throws IOException {
		return new ClassPathResource(classpathResource).getInputStream().readAllBytes();
	}

}