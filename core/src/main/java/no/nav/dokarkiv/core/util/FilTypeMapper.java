package no.nav.dokarkiv.core.util;

public class FilTypeMapper {
	public static String mapFiltype(String filtype) {
		if ("TIF".equals(filtype)) {
			return "TIFF";
		}
		if ("JPG".equals(filtype)) {
			return "JPEG";
		}
		return filtype;
	}
}
