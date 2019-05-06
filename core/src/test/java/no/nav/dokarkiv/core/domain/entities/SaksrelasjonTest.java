package no.nav.dokarkiv.core.domain.entities;

import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.junit.Test;

/**
 * Unit tests for Saksrelasjon.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class SaksrelasjonTest {

	@Test
	public void shouldThrowExceptionForMissingSakId() throws Exception {
		Saksrelasjon saksrelasjon = getSaksrelasjonBuilder()
										.fagsystem(FagsystemCode.PEN)
										.build();
		
		assertExceptionThrownWhenVerifyingMandatoryFields(saksrelasjon, "sakId");
	}
	
	@Test
	public void shouldThrowExceptionForMissingFagsystem() throws Exception {
		Saksrelasjon saksrelasjon = getSaksrelasjonBuilder()
										.sakId("123")
										.build();

		assertExceptionThrownWhenVerifyingMandatoryFields(saksrelasjon, "fagsystem");
	}
	
	@Test
	public void shouldThrowExceptionForMissingEndretAvNavn() throws Exception {
		Saksrelasjon saksrelasjon = getSaksrelasjonBuilder()
										.saksrelasjonId(10L)
										.sakId("123")
										.fagsystem(FagsystemCode.PEN)
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
