package no.nav.dokarkiv.core.domain.validator;

import no.nav.dokarkiv.core.domain.Bruker;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.exceptions.InvalidBrukerException;
import no.nav.dokarkiv.core.exceptions.InvalidOrgnrException;
import no.nav.dokarkiv.core.stelvio.Pid;

/**
 * Validates that a Bruker has a valid brukerId in the case Bruker is a Person
 * or an Organisasjon.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
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
		verifyBrukerIdIsValid(bruker);
	}

	private static void verifyBrukerIdIsValid(Bruker gjelderInfo) {
		validateFnrIfGjelderTypeIsPerson(gjelderInfo);
		validateOrgnrIfGjelderTypeIsOrg(gjelderInfo);
	}

	private static void validateOrgnrIfGjelderTypeIsOrg(Bruker bruker) {
		if (bruker.getBrukerType() == BrukerTypeCode.ORGANISASJON) {
			try {
				OrgnrValidator.validate(bruker.getBrukerId());
			} catch (InvalidOrgnrException e) {
				throw new InvalidBrukerException("BrukerId is not a valid orgnr: " + bruker.getBrukerId(), e);
			}
		}
	}

	private static void validateFnrIfGjelderTypeIsPerson(Bruker bruker) {
		if (bruker.getBrukerType() == BrukerTypeCode.PERSON) {
			if (!Pid.isValidPid(bruker.getBrukerId())) {
				throw new InvalidBrukerException("BrukerId is not a valid fnr: " + bruker.getBrukerId());
			}
		}
	}

}
