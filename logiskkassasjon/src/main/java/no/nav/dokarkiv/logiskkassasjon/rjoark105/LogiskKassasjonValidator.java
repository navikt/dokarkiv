package no.nav.dokarkiv.logiskkassasjon.rjoark105;

import static java.util.Objects.isNull;

import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import org.springframework.stereotype.Component;

@Component
public class LogiskKassasjonValidator {

	public void validerLogiskKassasjonRequest(Long dokumentInfoId) {
		if (isNull(dokumentInfoId)) {
			throw new UgyldigInputException("DokumentInfoId kan ikke være null");
		}
	}
}
