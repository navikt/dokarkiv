package no.nav.dokarkiv.core.domain.validator;

import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.exceptions.InvalidBrukerException;
import no.nav.dokarkiv.core.exceptions.InvalidOrgnrException;

import static no.nav.dokarkiv.core.domain.codes.BrukerTypeCode.ORGANISASJON;
import static no.nav.dokarkiv.core.domain.codes.BrukerTypeCode.PERSON;

/**
 * Validates that a Bruker has a valid brukerId in the case Bruker is a Person
 * or an Organisasjon.
 */
public final class BrukerValidator {

	/**
	 * Avoid instantiation.
	 */
	private BrukerValidator() {
	}

	/**
	 * Validate the bruker's brukerId
	 *
	 * @param bruker The Bruker.
	 */
	public static void validate(Bruker bruker) {
		validateBrukerId(bruker);
	}

	private static void validateBrukerId(Bruker bruker) {
		validateFnrIfBrukertypeIsPerson(bruker);
		validateOrgnrIfBrukertypeIsOrg(bruker);
	}

	private static void validateOrgnrIfBrukertypeIsOrg(Bruker bruker) {
		if (bruker.getBrukerType() == ORGANISASJON) {
			try {
				OrgnrValidator.validate(bruker.getBrukerId());
			} catch (InvalidOrgnrException e) {
				throw new InvalidBrukerException("BrukerId is not a valid orgnr: " + bruker.getBrukerId(), e);
			}
		}
	}

	private static void validateFnrIfBrukertypeIsPerson(Bruker bruker) {
		if (bruker.getBrukerType() == PERSON && !FoedselsnummerValidator.isValidPid(bruker.getBrukerId())) {
			throw new InvalidBrukerException("BrukerId is not a valid fnr.");
		}
	}

}