package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EksternReferanseIdFinnesException extends DokarkivFunctionalException {

	public EksternReferanseIdFinnesException(String message) {
		super(message);
	}
}
