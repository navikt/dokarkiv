package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.domain.entities.Journalpost.KANAL_REFERANSE_ID_LENGTH;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

public final class CommonValidator {
	private static final Pattern EKSTERN_REFERANSE_ID_PATTERN = Pattern.compile("[a-zA-Z0-9-._~!$&\"\\\\*+,;=:@]+");

	private CommonValidator() {
		//no-op
	}

	public static void validateId(String id, String feltnavn) {
		try {
			hasText(id, feltnavn);
			isNumeric(id, feltnavn);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("%s. %s=%s", e.getMessage(), feltnavn, id));
		}
	}

	public static void validateBoolean(Boolean value, String feltnavn) {
		if (value == null) {
			throw new InputValideringFeiletException(format("Feltet %s kan ikke være null eller tomt", feltnavn));
		}
	}

	public static void validateJournalfoerendeEnhet(String journalfoerendeEnhet, String feltnavn) {
		try {
			hasText(journalfoerendeEnhet, feltnavn);
			hasLength(journalfoerendeEnhet, feltnavn, 4);
			isNumeric(journalfoerendeEnhet, feltnavn);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("%s. journalfoerendeEnhet=%s", e.getMessage(), journalfoerendeEnhet));
		}
	}

	public static void hasText(String input, String feltnavn) {
		if (StringUtils.isBlank(input)) {
			throw new IllegalArgumentException(format("Feltet %s kan ikke være null eller tomt", feltnavn));
		}
	}

	public static void validateEksternReferanseId(String eksternReferanseId) {
		if(isEmpty(eksternReferanseId)){
			throw new InputValideringFeiletException("eksternReferanseId kan ikke være null eller tomt");
		} else {
			if (eksternReferanseId.length() > KANAL_REFERANSE_ID_LENGTH) {
				throw new InputValideringFeiletException(format("eksternReferanseId kan ikke være over %d tegn. Mottatt eksternReferanseId=%s", KANAL_REFERANSE_ID_LENGTH, eksternReferanseId));
			}
			if (!EKSTERN_REFERANSE_ID_PATTERN.matcher(eksternReferanseId).matches()) {
				throw new InputValideringFeiletException(format("eksternReferanseId kan bare inneholde alfanumeriske tegn og følgende spesialtegn :;,.=-_~$&+*\"\\@! Mottatt eksternReferanseId=%s", eksternReferanseId));
			}
		}
	}

	private static void hasLength(String input, String feltnavn, int length) {
		if (input.length() != length) {
			throw new IllegalArgumentException(format("Feltet %s må ha lengde=%d, men har lengde=%s", feltnavn, length, input.length()));
		}
	}

	private static void isNumeric(String input, String feltnavn) {
		if (!StringUtils.isNumeric(input)) {
			throw new IllegalArgumentException(format("Feltet %s må være et heltall. Mottatt verdi=%s", feltnavn, input));
		}
	}

	public static void validateNotNull(Object o, String feltnavn) {
		validateNotNull(o, feltnavn, null);
	}

	public static void validateNotNull(Object o, String feltnavn, String ekstraInformasjon) {
		if (o == null) {
			throw new InputValideringFeiletException(feltnavn + " kan ikke være null" + (ekstraInformasjon != null ? ", " + ekstraInformasjon : "") + "!");
		}
	}
}
