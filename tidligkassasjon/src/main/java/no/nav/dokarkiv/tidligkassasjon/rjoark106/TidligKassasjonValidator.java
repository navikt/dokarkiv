package no.nav.dokarkiv.tidligkassasjon.rjoark106;

import static java.util.Objects.isNull;

import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import org.springframework.stereotype.Component;

@Component
public class TidligKassasjonValidator {

	public void validerTidligKassasjonRequest(Long dokumentInfoId) {
		if (isNull(dokumentInfoId)) {
			throw new UgyldigInputException("DokumentInfoId kan ikke være null");
		}
	}
}
