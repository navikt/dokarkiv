package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ConsumerIsNotSrvDokarkivProxyFunctionalException extends DokarkivFunctionalException {
	public ConsumerIsNotSrvDokarkivProxyFunctionalException(String message) {
		super(message);
	}
}
