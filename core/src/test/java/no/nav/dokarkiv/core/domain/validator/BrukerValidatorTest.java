package no.nav.dokarkiv.core.domain.validator;

import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.exceptions.InvalidBrukerException;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.codes.BrukerTypeCode.ORGANISASJON;
import static no.nav.dokarkiv.core.domain.codes.BrukerTypeCode.PERSON;
import static no.nav.dokarkiv.core.domain.codes.BrukerTypeCode.SAMHANDLER;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

public class BrukerValidatorTest {

	private static final String VALID_FNR = "01014138923";
	private static final String INVALID_FNR = "01014138924";
	private static final String VALID_ORGNR = "123456785";
	private static final String SOME_GJELDERID = "aaaaa";
	private static final String VALID_TESTNORGE_IDENT = "27857798800";

	@Test
	public void shouldValidateBrukerIdForPerson() {
		Bruker bruker = getBrukerBuilder()
				.brukerId(VALID_FNR)
				.brukerType(PERSON)
				.build();

		assertThatNoException()
				.isThrownBy(() -> BrukerValidator.validate(bruker));
	}

	@Test
	public void shouldValidateTestNorgePerson() {
		Bruker bruker = getBrukerBuilder()
				.brukerId(VALID_TESTNORGE_IDENT)
				.brukerType(PERSON)
				.build();

		assertThatNoException()
				.isThrownBy(() -> BrukerValidator.validate(bruker));
	}

	@Test
	public void shouldValidateBrukerIdForOrg() {
		Bruker bruker = getBrukerBuilder()
				.brukerId(VALID_ORGNR)
				.brukerType(ORGANISASJON)
				.build();

		assertThatNoException()
				.isThrownBy(() -> BrukerValidator.validate(bruker));
	}

	@Test
	public void shouldValidateBrukerIdForSamhandler() {
		Bruker bruker = getBrukerBuilder()
				.brukerId(SOME_GJELDERID)
				.brukerType(SAMHANDLER)
				.build();

		assertThatNoException()
				.isThrownBy(() -> BrukerValidator.validate(bruker));
	}

	@Test
	public void shouldNotValidateBrukerIdForPersonWithInvalidFnr() {
		Bruker bruker = getBrukerBuilder()
				.brukerId(INVALID_FNR)
				.brukerType(PERSON)
				.build();


		assertThatExceptionOfType(InvalidBrukerException.class)
				.isThrownBy(() -> BrukerValidator.validate(bruker))
				.withMessage("BrukerId is not a valid fnr.");
	}

}