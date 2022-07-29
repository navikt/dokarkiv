package no.nav.dokarkiv.core.domain.entities;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for Bruker.
 *
 */
public class BrukerTest {

	@Test
	public void shouldThrowExceptionForMissingBrukerId() throws Exception {
		Bruker bruker = getBrukerBuilder()
				.brukerType(BrukerTypeCode.PERSON)
				.build();

		assertExceptionThrownWhenVerifyingMandatoryFields(bruker, "brukerId");
	}

	@Test
	public void shouldThrowExceptionForMissingBrukerType() throws Exception {
		Bruker bruker = getBrukerBuilder()
				.brukerId("12312312312")
				.build();

		assertExceptionThrownWhenVerifyingMandatoryFields(bruker, "brukerType");
	}

	private void assertExceptionThrownWhenVerifyingMandatoryFields(Bruker bruker, String fieldName) {
		try {
			bruker.verifyMandatoryFields();
			fail();
		} catch (InvalidArgumentException e) {
			assertThat(e.getMessage(), containsString(fieldName));
		}
	}

}
