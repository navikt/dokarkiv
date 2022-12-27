package no.nav.dokarkiv.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for MimeTypeMapper
 */
public class MimeTypeMapperTest {

	private final MimeTypeMapper mapper = new MimeTypeMapper();

	@Test
	public void shouldGetMimeTypeForPdf() {
		assertExpectedMimeTypeForFileExtension("application/pdf", "PDF");
	}

	@Test
	public void shouldGetMimeTypeForPdfA() {
		assertExpectedMimeTypeForFileExtension("application/pdf", "PDFA");
	}

	@Test
	public void shouldGetMimeTypeForPdfa() {
		assertExpectedMimeTypeForFileExtension("application/pdf", "PDFA");
	}

	@Test
	public void shouldGetMimeTypeForXml() {
		assertExpectedMimeTypeForFileExtension("text/xml", "xml");
	}

	@Test
	public void shouldGetMimeTypeForDXml() {
		assertExpectedMimeTypeForFileExtension("text/xml", "dxml");
	}

	@Test
	public void shouldGetMimeTypeForAXml() {
		assertExpectedMimeTypeForFileExtension("text/xml", "axml");
	}

	@Test
	public void shouldGetMimeTypeForRtf() {
		assertExpectedMimeTypeForFileExtension("application/rtf", "rtf");
	}

	@Test
	public void shouldGetMimeTypeForAfp() {
		assertExpectedMimeTypeForFileExtension("application/afp", "afp");
	}

	@Test
	public void shouldGetMimeTypeForMeta() {
		assertExpectedMimeTypeForFileExtension("text/xml", "meta");
	}

	@Test
	public void shouldGetMimeTypeForDlf() {
		assertExpectedMimeTypeForFileExtension("application/dlf", "dlf");
	}

	@Test
	public void shouldGetMimeTypeForJpeg() {
		assertExpectedMimeTypeForFileExtension("image/jpeg", "jpeg");
	}

	@Test
	public void shouldGetMimeTypeForTiff() {
		assertExpectedMimeTypeForFileExtension("image/tiff", "tiff");
	}

	@Test
	public void shouldGetMimeTypeForDoc() {
		assertExpectedMimeTypeForFileExtension("application/msword", "doc");
	}

	@Test
	public void shouldGetMimeTypeForDocx() {
		assertExpectedMimeTypeForFileExtension("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
				"docx");
	}

	@Test
	public void shouldGetMimeTypeForXls() {
		assertExpectedMimeTypeForFileExtension("application/vnd.ms-excel", "xls");
	}

	@Test
	public void shouldGetMimeTypeForXlsx() {
		assertExpectedMimeTypeForFileExtension("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
	}

	@Test
	public void shouldGetApplicationBinaryForUnknownFileExtension() {
		assertExpectedMimeTypeForFileExtension("application/binary", "unknownextension");
	}

	private void assertExpectedMimeTypeForFileExtension(String expectedMimeType, String extension) {
		String mimeType = mapper.getMimeTypeForFileExtension(extension);
		assertEquals(expectedMimeType, mimeType);
	}
}
