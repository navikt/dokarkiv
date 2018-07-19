package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class ValideringAvVedleggFeiletException extends FunctionalRecoverableException {

	public ValideringAvVedleggFeiletException(String message) {
		super(message);
	}
}
