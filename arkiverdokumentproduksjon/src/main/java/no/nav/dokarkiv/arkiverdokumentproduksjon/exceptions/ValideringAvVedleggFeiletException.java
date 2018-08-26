package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class ValideringAvVedleggFeiletException extends DokarkivFunctionalException {

	public ValideringAvVedleggFeiletException(String message) {
		super(message);
	}
}
