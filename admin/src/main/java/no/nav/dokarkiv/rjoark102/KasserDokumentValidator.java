package no.nav.dokarkiv.rjoark102;

import static java.util.Objects.isNull;

import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.dto.KasserDokumentRequest;
import org.springframework.stereotype.Component;

@Component
public class KasserDokumentValidator {

	public void validerKasserDokumentRequest(KasserDokumentRequest request) {
		if (isNull(request.getDokumentInfoId())) {
			throw new UgyldigInputException("DokumentInfoId kan ikke være null");
		}
		if (isNull(request.getKassertAvNavn())) {
			throw new UgyldigInputException("KassertAvNavn kan ikke være null");
		}
	}
}
