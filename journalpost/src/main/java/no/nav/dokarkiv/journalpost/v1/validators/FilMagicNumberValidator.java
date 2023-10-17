package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Set;

import static java.lang.Enum.valueOf;
import static java.util.Arrays.copyOf;
import static java.util.HexFormat.of;

public class FilMagicNumberValidator {
	public static final byte[] PDF_MAGIC_NUMBER = of().parseHex("255044462D");
	public static final Set<byte[]> JPEG_MAGIC_NUMBERS = Set.of(
			of().parseHex("FFD8FFDB"),
			of().parseHex("FFD8FFEE"),
			of().parseHex("FFD8FFE0"),
			of().parseHex("0000000C6A5020200D0A870A"),
			of().parseHex("FF4FFF51")
	);
	public static final byte[] PNG_MAGIC_NUMBER = HexFormat.of().parseHex("89504E470D0A1A0A");

	public static boolean isFileContentContainsValidMagicNumber(String fileType, final byte[] fileContent) {
		FilTypeCode filTypeCode = valueOf(FilTypeCode.class, fileType);
		return switch (filTypeCode) {
			case PDF, PDFA -> isValidPdfMagicNumber(fileContent);
			case JPEG -> isValidJpegMagicNumber(fileContent);
			case PNG -> isValidPngMagicNumber(fileContent);
			default -> true;
		};
	}

	private static boolean isValidJpegMagicNumber(final byte[] fileContent) {
		return JPEG_MAGIC_NUMBERS.stream()
				.anyMatch(jpegMagicNumber -> {
					byte[] byteArray = Arrays.copyOf(fileContent, jpegMagicNumber.length);
					return Arrays.equals(jpegMagicNumber, byteArray);
				});
	}

	private static boolean isValidPngMagicNumber(final byte[] fileContent) {
		return Arrays.equals(copyOf(fileContent, PNG_MAGIC_NUMBER.length), PNG_MAGIC_NUMBER);
	}

	private static boolean isValidPdfMagicNumber(final byte[] fileContent) {
		boolean isPdf = Arrays.equals(copyOf(fileContent, PDF_MAGIC_NUMBER.length), PDF_MAGIC_NUMBER);
		if (!isPdf) {
			// ISO 32000-1:2008
			// Ref 7.5.2 - The first line of a PDF file shall be a header consisting of the 5 characters %PDF-
			// followed by a version number of the form 1.N, where N is a digit between 0 and 7.
			// Noen PDF har denne headeren et sted i de neste 1024 bytes
			return isValidPdfMagicNumberInFirst1024Bytes(fileContent);
		}
		return true;
	}

	private static boolean isValidPdfMagicNumberInFirst1024Bytes(final byte[] fileContent) {
		byte[] buffer = copyOf(fileContent, 1024);
		return findByteArrayInByteArray(buffer, PDF_MAGIC_NUMBER);
	}

	private static boolean findByteArrayInByteArray(final byte[] fileContent, byte[] key) {
		for (int i = 0; i <= fileContent.length - key.length; i++) {
			int j = 0;
			while (j < key.length && fileContent[i + j] == key[j]) {
				j++;
			}
			if (j == key.length) {
				return true;
			}
		}
		return false;
	}

}
