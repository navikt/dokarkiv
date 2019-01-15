package no.nav.dokarkiv.fysisktidligkassasjon.rjoark107;

import static java.util.Objects.isNull;

import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import org.springframework.stereotype.Component;

@Component
public class FysiskTidligKassasjonValidator {

	public void validerFysiskTidligKassasjonRequest(Long dokumentInfoId) {
		if (isNull(dokumentInfoId)) {
			throw new UgyldigInputException("DokumentInfoId kan ikke være null");
		}
	}
}
