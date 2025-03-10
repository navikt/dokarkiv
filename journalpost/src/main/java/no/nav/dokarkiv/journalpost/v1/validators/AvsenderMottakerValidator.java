package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hibernate.internal.util.StringHelper.isBlank;

public class AvsenderMottakerValidator {
	private static final Pattern FNR_PATTERN = Pattern.compile("^\\d{11}$");
	private static final Pattern ORGNR_PATTERN = Pattern.compile("^\\d{9}$");
	private static final Pattern HPRNR_PATTERN = Pattern.compile("^\\d{7,9}$");

	public static List<String> validateAvsenderMottaker(AvsenderMottaker avsenderMottaker){
		List<String> feilmeldinger = new ArrayList<>();
		if (avsenderMottakerIdOgIdTypeSkalNulles(avsenderMottaker)) 
			return feilmeldinger;

		feilmeldinger.add(validateAvsenderMottakerIdOgIdType(avsenderMottaker));
		feilmeldinger.add(validateAvsenderMottakerId(avsenderMottaker));

		return feilmeldinger;
	}

	private static boolean avsenderMottakerIdOgIdTypeSkalNulles(AvsenderMottaker avsenderMottaker) {
		return " ".equals(avsenderMottaker.getId());
	}

	private static String validateAvsenderMottakerIdOgIdType(AvsenderMottaker avsenderMottaker) {
		if (isBlank(avsenderMottaker.getId()) && avsenderMottaker.getIdType() != null) {
			return format("Oppdatering av avsenderMottaker.idType krever at feltet avsenderMottaker.id er satt. Mottatt id=%s idType=%s",
					masker(avsenderMottaker.getId()),
					avsenderMottaker.getIdType());
		} else if (isNotEmpty(avsenderMottaker.getId()) && avsenderMottaker.getIdType() == null) {
			return format("Oppdatering av avsenderMottaker.id krever at feltet avsenderMottaker.idType er satt. Mottatt id=%s idType=null",
					masker(avsenderMottaker.getId()));
		}
		return null;
	}

	private static String validateAvsenderMottakerId(AvsenderMottaker avsenderMottaker) {
		if (avsenderMottaker.getIdType() != null && avsenderMottaker.getId() != null) {
			switch (avsenderMottaker.getIdType()) {
				case FNR -> {
					if (!FNR_PATTERN.matcher(avsenderMottaker.getId()).matches()) {
						return "avsenderMottaker.id må være 11 siffer dersom avsenderMottaker.idType=FNR.";
					}
				}
				case ORGNR -> {
					if (!ORGNR_PATTERN.matcher(avsenderMottaker.getId()).matches()) {
						return "avsenderMottaker.id må være 9 siffer dersom avsenderMottaker.idType=ORGNR.";
					}
				}
				case HPRNR -> {
					if (!HPRNR_PATTERN.matcher(avsenderMottaker.getId()).matches()) {
						return "avsenderMottaker.id må være 7-9 siffer dersom avsenderMottaker.idType=HPRNR.";
					}
				}
			}
		}
		return null;
	}

	private static String masker(String s) {
		if (s == null) {
			return null;
		}
		return s.substring(0, s.length() / 2) + "*".repeat(s.length() / 2);
	}
}
