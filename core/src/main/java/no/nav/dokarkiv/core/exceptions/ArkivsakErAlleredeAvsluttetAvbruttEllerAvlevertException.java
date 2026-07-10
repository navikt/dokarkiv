package no.nav.dokarkiv.core.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.CONFLICT;

@ResponseStatus(code = CONFLICT)
public class ArkivsakErAlleredeAvsluttetAvbruttEllerAvlevertException extends DokarkivFunctionalException {

	public ArkivsakErAlleredeAvsluttetAvbruttEllerAvlevertException(String message) {
		super(message);
	}

}