package no.nav.dokarkiv.journalfoerinngaaende.v1.util;

import no.nav.dok.tjenester.journalfoerinngaaende.Dokument;
import no.nav.dok.tjenester.journalfoerinngaaende.GetJournalpostResponse;
import no.nav.dokarkiv.core.exceptions.DokarkivRestFunctionalException;
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
	public static List<String> getDokumentIds(GetJournalpostResponse response) {
		return response.getDokumentListe().stream()
				.map(Dokument::getDokumentId)
				.collect(Collectors.toList());
	}

	public static List<String> getDokumenttypeIds(GetJournalpostResponse response) {
		return response.getDokumentListe().stream()
				.filter(dokumentinfoTo -> dokumentinfoTo.getDokumentTypeId() != null)
				.map(Dokument::getDokumentTypeId)
				.collect(Collectors.toList());
	}
}
