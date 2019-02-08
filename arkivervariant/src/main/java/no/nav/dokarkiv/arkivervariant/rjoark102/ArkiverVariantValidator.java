package no.nav.dokarkiv.arkivervariant.rjoark102;

import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
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

		if (Objects.isNull(request.getFilType())) {
			throw new UgyldigInputException("Filtype kan ikke være null");
		}

		if (Objects.isNull(request.getFil())) {
			throw new UgyldigInputException("Fil kan ikke være null");
		}
		if (Objects.isNull(request.getFilnavn())) {
			throw new UgyldigInputException("Filnavn kan ikke være null");
		}
	}
}
