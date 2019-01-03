package no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103;

import no.nav.dokarkiv.arkiverkorrigertdokument.exception.UgyldigInputException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ArkiverKorrigertDokumentValidator {

	public void validateArkiverKorrigertDokumentRequest(ArkiverKorrigertDokumentRequest request) {
		if (Objects.isNull(request.getDokumentInfoId())) {
			throw new UgyldigInputException("DokumentInfoId kan ikke være null");
		}

		if (Objects.isNull(request.getFil())) {
			throw new UgyldigInputException("Fil kan ikke være null");
		}
	}

	public void validateAngreArkiverKorrigertDokument(Long dokumentInfoId) {
		if (dokumentInfoId == null) {
			throw new UgyldigInputException("DokumentInfoId kan ikke være null");
		}
	}
}
