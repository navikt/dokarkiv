package no.nav.dokarkiv.core.domain.entities;

import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for Saksrelasjon.
 */
public class SaksrelasjonTest {

	@Test
	public void shouldThrowExceptionForMissingSakId() {
		Saksrelasjon saksrelasjon = getSaksrelasjonBuilder()
				.fagsystem(FagsystemCode.FS22)
				.build();

		assertExceptionThrownWhenVerifyingMandatoryFields(saksrelasjon, "sakId");
	}

	@Test
	public void shouldThrowExceptionForMissingFagsystem() {
		Saksrelasjon saksrelasjon = getSaksrelasjonBuilder()
				.saknrfk("123")
				.build();

		assertExceptionThrownWhenVerifyingMandatoryFields(saksrelasjon, "fagsystem");
	}

	@Test
	public void shouldThrowExceptionForMissingEndretAvNavn() {
		Saksrelasjon saksrelasjon = getSaksrelasjonBuilder()
				.saksrelasjonId(10L)
				.saknrfk("123")
				.fagsystem(FagsystemCode.FS22)
				.build();

		assertExceptionThrownWhenVerifyingMandatoryFields(saksrelasjon, "endretAvNavn");
	}

	private void assertExceptionThrownWhenVerifyingMandatoryFields(Saksrelasjon saksrelasjon, String fieldName) {
		try {
			saksrelasjon.verifyMandatoryFields();
			fail();
		} catch (InvalidArgumentException e) {
			assertThat(e.getMessage(), containsString(fieldName));
		}
	}

}
