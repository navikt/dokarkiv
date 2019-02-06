package no.nav.dokarkiv.arkivervariant.rjoark102;

import no.nav.dokarkiv.arkivervariant.exception.UgyldigInputException;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ArkiverVariantValidator {

	public void validateArkiverVariantRequest(Long dokumentInfoId, String variant, String fil) {
		if (Objects.isNull(dokumentInfoId)) {
			throw new UgyldigInputException("DokumentInfoId kan ikke være null");
		}

		if (Objects.isNull(variant)) {
			throw new UgyldigInputException("Variant kan ikke være null");
		}

		try {
			VariantFormatCode.valueOf(variant);
		} catch (IllegalArgumentException e) {
			throw new UgyldigInputException(String.format("Varianten: %s er ugyldig", variant));
		}

		if (Objects.isNull(fil)) {
			throw new UgyldigInputException("Fil kan ikke være null");
		}
	}
}
