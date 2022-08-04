package no.nav.dokarkiv.core.domain.validator;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.exceptions.InvalidBrukerException;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests BrukerValidator.
 */
public class BrukerValidatorTest {

	private static final String SOME_VALID_FNR = "01014138923";
	private static final String SOME_INVALID_FNR = "01014138924";
	private static final String SOME_VALID_ORGNR = "123456785";
	private static final String SOME_GJELDERID = "aaaaa";
	private static final String TESTNORGE_INDENT = "27857798800";

	@Test
	public void shouldValidateValidGjelderInfoForPerson() {
		Bruker bruker = getBrukerBuilder()
				.brukerId(SOME_VALID_FNR)
				.brukerType(BrukerTypeCode.PERSON)
				.build();

		assertBrukerIsValid(bruker);
	}

	@Test
	public void shouldValidateValidTestNorgePerson() {
		Bruker bruker = getBrukerBuilder()
				.brukerId(TESTNORGE_INDENT)
				.brukerType(BrukerTypeCode.PERSON)
				.build();

		assertBrukerIsValid(bruker);
	}

	@Test
	public void shouldValidateValidGjelderInfoForOrg() {
		Bruker bruker = getBrukerBuilder()
				.brukerId(SOME_VALID_ORGNR)
				.brukerType(BrukerTypeCode.ORGANISASJON)
				.build();

		assertBrukerIsValid(bruker);
	}

	@Test
	public void shouldValidateGjelderInfoForSamhandler() {
		Bruker bruker = getBrukerBuilder()
				.brukerId(SOME_GJELDERID)
				.brukerType(BrukerTypeCode.SAMHANDLER)
				.build();

		assertBrukerIsValid(bruker);
	}

	@Test
	public void shouldNotValidateGjelderInfoForPersonWithInvalidFnr() {
		Bruker bruker = getBrukerBuilder()
				.brukerId(SOME_INVALID_FNR)
				.brukerType(BrukerTypeCode.PERSON)
				.build();


		assertGjelderInfoValidationFails(bruker, "Validation should fail for invalid fnr");
	}

	private void assertGjelderInfoValidationFails(Bruker bruker, String errorMessage) {
		try {
			BrukerValidator.validate(bruker);
			fail(errorMessage);
		} catch (InvalidBrukerException e) {
		}
	}

	private void assertBrukerIsValid(Bruker bruker) {
		BrukerValidator.validate(bruker);
	}

}
