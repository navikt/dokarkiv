package no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class FerdigstillingIkkeMuligException extends DokarkivFunctionalException {

	public FerdigstillingIkkeMuligException(String message) {
		super(message);
	}

	public FerdigstillingIkkeMuligException(String message, Throwable cause) {
		super(message, cause);
	}
}
