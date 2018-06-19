package no.nav.dokarkiv.core.domain.entities;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.junit.Test;

/**
 * Unit tests for Bruker.
 * 
 * @author Thomas Eugen Bjørge, Visma Sirius
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
						.brukerId("***gammelt_fnr***")
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
