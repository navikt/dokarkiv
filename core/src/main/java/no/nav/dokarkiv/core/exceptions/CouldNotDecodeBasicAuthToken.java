package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class CouldNotDecodeBasicAuthToken extends DokarkivFunctionalException {
	public CouldNotDecodeBasicAuthToken(String message){
		super(message);
	}
}
