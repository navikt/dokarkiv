package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.HexFormat;
import java.util.stream.Stream;

import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.JPEG;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDF;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PNG;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.XML;
import static no.nav.dokarkiv.journalpost.v1.validators.FilMagicNumberValidator.isFileContentContainsValidMagicNumber;
import static org.assertj.core.api.Assertions.assertThat;

class FilMagicNumberValidatorTest {

	@ParameterizedTest
	@MethodSource
	public void shouldReturnTrueWhenContainsValidMagicNumber(byte[] filContenet, FilTypeCode filType) {
		assertThat(isFileContentContainsValidMagicNumber(filType.name(), filContenet)).isTrue();
	}

	@ParameterizedTest
	@MethodSource
	public void shouldReturnFalseWhenContainsInvalidMagicNumber(byte[] filContenet, FilTypeCode filType) {
		assertThat(isFileContentContainsValidMagicNumber(filType.name(), filContenet)).isFalse();
	}

	private static Stream<Arguments> shouldReturnTrueWhenContainsValidMagicNumber() throws IOException {
		byte[] pdfFile = classpathToInputStream("pdf/pdf/453644598_skan_im_pdfa.pdf");
		byte[] jpegFile = classpathToInputStream("jpeg/2021_01_06_nasjonale_tiltak_16_9.jpg");
		byte[] pngFile = classpathToInputStream("png/2021_01_06_nasjonale_tiltak.png");
		return Stream.of(
				Arguments.of(pdfFile, PDF),
				Arguments.of(jpegFile, JPEG),
				Arguments.of(HexFormat.of().parseHex("0000000C6A5020200D0A870AFFD8FFEE"), JPEG),
				Arguments.of(pngFile, PNG),
				Arguments.of(null, XML)
		);
	}

	private static Stream<Arguments> shouldReturnFalseWhenContainsInvalidMagicNumber() throws IOException {
		byte[] pdfFile = classpathToInputStream("pdf/pdf/2021_01_06_nasjonale_tiltak_feil.pdf");
		byte[] jpegFile = classpathToInputStream("jpeg/2021_01_06_nasjonale_tiltak_feil.jpg");
		byte[] pngFile = classpathToInputStream("png/2021_01_06_nasjonale_tiltak_feil.png");
		return Stream.of(
				Arguments.of(pdfFile, PDF),
				Arguments.of(jpegFile, JPEG),
				Arguments.of(HexFormat.of().parseHex("230000000C6A5020200D0A870AFFD8FFEE"), JPEG),
				Arguments.of(pngFile, PNG)
		);
	}

	private static byte[] classpathToInputStream(String classpathResource) throws IOException {
		return new ClassPathResource(classpathResource).getInputStream().readAllBytes();
	}

}