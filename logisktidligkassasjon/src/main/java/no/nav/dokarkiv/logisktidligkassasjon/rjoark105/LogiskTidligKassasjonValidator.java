package no.nav.dokarkiv.logisktidligkassasjon.rjoark105;

import static java.util.Objects.isNull;

import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import org.springframework.stereotype.Component;

@Component
public class LogiskTidligKassasjonValidator {

	public void validerLogiskTidligKassasjonRequest(Long dokumentInfoId) {
		if (isNull(dokumentInfoId)) {
			throw new UgyldigInputException("DokumentInfoId kan ikke være null");
		}
	}
}
