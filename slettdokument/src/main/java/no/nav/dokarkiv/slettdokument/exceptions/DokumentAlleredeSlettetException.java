package no.nav.dokarkiv.slettdokument.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

public class DokumentAlleredeSlettetException extends DokarkivFunctionalException {

	public DokumentAlleredeSlettetException(String message) {
		super(message);
	}
}
