package no.nav.dokarkiv.journalfoerInngaaende.v1.util;

import no.nav.dokarkiv.core.exceptions.DokarkivRestFunctionalException;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.JournalpostResponseTo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class Utils {

	public static Long convertStringToLong(String input, String feltnavn) throws DokarkivRestFunctionalException {
		try {
			return Long.parseLong(input);
		} catch (Exception e) {
			throw new DokarkivRestFunctionalException(String.format("%s er ikke et tall", feltnavn), HttpStatus.BAD_REQUEST);
		}
	}

	public static void hasText(String input, String feltnavn) {
		if (StringUtils.isBlank(input)) {
			throw new DokarkivRestFunctionalException(String.format("%s kan ikke være null eller tom", feltnavn), HttpStatus.BAD_REQUEST);
		}
	}

	//Used for logging
	public static List<String> getDokumentIds(JournalpostResponseTo responseTo) {
		return responseTo
				.getDokumenter()
				.stream()
				.map(dokumentinfoTo -> new String(dokumentinfoTo.getDokumentId())).collect(Collectors.toList());
	}

	public static List<String> getDokumenttypeIds(JournalpostResponseTo responseTo) {
		return responseTo
				.getDokumenter()
				.stream()
				.filter(dokumentinfoTo -> dokumentinfoTo.getDokumenttypeId() != null)
				.map(dokumentinfoTo -> new String(dokumentinfoTo.getDokumenttypeId())).collect(Collectors.toList());
	}
}
