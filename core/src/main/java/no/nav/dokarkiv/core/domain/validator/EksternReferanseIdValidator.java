package no.nav.dokarkiv.core.domain.validator;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;

import java.util.regex.Pattern;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.domain.entities.Journalpost.KANAL_REFERANSE_ID_LENGTH;
import static org.apache.commons.lang3.StringUtils.isBlank;

public final class EksternReferanseIdValidator {

	public static final Pattern EKSTERN_REFERANSE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9-._~!$&\"\\\\*+,;=:@]+$");

	private EksternReferanseIdValidator() {
		//no-op
	}

	public static void validateEksternReferanseId(String eksternReferanseId) {
		if (isBlank(eksternReferanseId)) {
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
}
