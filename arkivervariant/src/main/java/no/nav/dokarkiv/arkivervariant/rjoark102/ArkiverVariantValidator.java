package no.nav.dokarkiv.arkivervariant.rjoark102;

import no.nav.dokarkiv.arkivervariant.exception.UgyldigInputException;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ArkiverVariantValidator {

	public void validateArkiverVariantRequest(ArkiverVariantRequest request) {
		if (Objects.isNull(request.getDokumentInfoId())) {
			throw new UgyldigInputException("DokumentInfoId kan ikke være null");
		}

		if (Objects.isNull(request.getVariant())) {
			throw new UgyldigInputException("Variant kan ikke være null");
		}

		try {
			VariantFormatCode.valueOf(request.getVariant());
		} catch (IllegalArgumentException e) {
			throw new UgyldigInputException(String.format("Varianten: %s er ugyldig", request.getVariant()));
		}

		if (Objects.isNull(request.getFilType())) {
			throw new UgyldigInputException("Filtype kan ikke være null");
		}

		try {
			FilTypeCode.valueOf(request.getFilType());
		} catch (IllegalArgumentException e) {
			throw new UgyldigInputException(String.format("Filtypen: %s er ugyldig", request.getFilType()));
		}

		if (Objects.isNull(request.getFil())) {
			throw new UgyldigInputException("Fil kan ikke være null");
		}
		if (Objects.isNull(request.getFilnavn())) {
			throw new UgyldigInputException("Filnavn kan ikke være null");
		}
	}
}
