package no.nav.dokarkiv.core.util;

import no.nav.dokarkiv.core.domain.entities.FilDetaljer;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Set;

import static java.util.Arrays.copyOf;
import static java.util.HexFormat.of;

public class FilMagicNumberValidator {
	public static final byte[] PDF_MAGIC_NUMBER = of().parseHex("255044462D");
	public static final Set<byte[]> JPEG_MAGIC_NUMBERS = Set.of(of().parseHex("FFD8FFDB"), of().parseHex("FFD8FFEE"), of().parseHex("FFD8FFE0"), of().parseHex("0000000C6A5020200D0A870A"), of().parseHex("FF4FFF51"));
	public static final byte[] PNG_MAGIC_NUMBER = HexFormat.of().parseHex("89504E470D0A1A0A");

	public static boolean isFileMagicNumberValid(FilDetaljer filDetaljer) {
		return switch (filDetaljer.getFiltype()) {
			case PDF, PDFA ->
					Arrays.equals(copyOf(filDetaljer.getFileContent(), PDF_MAGIC_NUMBER.length), PDF_MAGIC_NUMBER);
			case JPEG -> JPEG_MAGIC_NUMBERS.stream()
					.map(jpegMagicNumber -> {
						byte[] byteArray = Arrays.copyOf(filDetaljer.getFileContent(), jpegMagicNumber.length);
						return Arrays.equals(jpegMagicNumber, byteArray);
					}).anyMatch(result -> result == true);
			case PNG -> Arrays.equals(copyOf(filDetaljer.getFileContent(), PNG_MAGIC_NUMBER.length), PNG_MAGIC_NUMBER);
			default -> false;
		};
	}
}
