package no.nav.dokarkiv.core.domain;

import static no.nav.dokarkiv.core.domain.builder.KryssreferanseBuilder.getKryssreferanseBuilder;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import no.nav.dokarkiv.core.domain.codes.ReferanseTypeCode;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.junit.Test;

/**
 * Unit tests for Kryssreferanse.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class KryssreferanseTest {

	@Test
	public void shouldThrowExceptionForMissingReferanseId() throws Exception {
		Kryssreferanse kryssreferanse = getKryssreferanseBuilder()
										.referanseType(ReferanseTypeCode.SED_FLYT)
										.build();
		
		assertExceptionThrownWhenVerifyingMandatoryFields(kryssreferanse, "referanseId");
	}
	
	@Test
	public void shouldThrowExceptionForMissingReferanseType() throws Exception {
		Kryssreferanse kryssreferanse = getKryssreferanseBuilder()
										.referanseId("123")
										.build();
		
		assertExceptionThrownWhenVerifyingMandatoryFields(kryssreferanse, "referanseType");
	}
	
	private void assertExceptionThrownWhenVerifyingMandatoryFields(Kryssreferanse kryssreferanse, String fieldName) {
		try {
			kryssreferanse.verifyMandatoryFields();
			fail();
		} catch (InvalidArgumentException e) {
			assertThat(e.getMessage(), containsString(fieldName));
		}
	}
	
}
