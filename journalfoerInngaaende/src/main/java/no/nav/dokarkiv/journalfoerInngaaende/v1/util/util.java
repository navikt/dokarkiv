package no.nav.dokarkiv.journalfoerInngaaende.v1.util;

import no.nav.dokarkiv.core.exceptions.DokmotArkivRestFunctionalException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class util {

	public static Long convertStringToLong(String input, String feltnavn) throws DokmotArkivRestFunctionalException {
		try {
			return Long.parseLong(input);
		} catch (Exception e) {
			throw new DokmotArkivRestFunctionalException(String.format("%s er ikke et tall", feltnavn), HttpStatus.BAD_REQUEST);
		}
	}

	public static void hasText(String input, String feltnavn) {
		if (StringUtils.isBlank(input)) {
			throw new DokmotArkivRestFunctionalException(String.format("%s kan ikke være null eller tom", feltnavn), HttpStatus.BAD_REQUEST);
		}
	}
}
