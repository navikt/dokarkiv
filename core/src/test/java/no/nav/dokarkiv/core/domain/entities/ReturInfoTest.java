package no.nav.dokarkiv.core.domain.entities;

import static no.nav.dokarkiv.core.domain.builder.ReturInfoBuilder.getReturInfoBuilder;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import no.nav.dokarkiv.core.domain.codes.ArsakReturCode;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.junit.Test;

import java.util.Date;

/**
 * Unit tests for ReturInfo.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class ReturInfoTest {

	@Test
	public void shouldThrowExceptionForMissingReturDato() throws Exception {
		ReturInfo returInfo = getReturInfoBuilder()
								.arsakRetur(ArsakReturCode.IKKE_HENTET)
								.build();
		
		assertExceptionThrownWhenVerifyingMandatoryFields(returInfo, "returDato");
	}
	
	@Test
	public void shouldThrowExceptionForMissingArsakRetur() throws Exception {
		ReturInfo returInfo = getReturInfoBuilder()
								.returDato(new Date())
								.build();
		
		assertExceptionThrownWhenVerifyingMandatoryFields(returInfo, "arsakRetur");
	}
	
	private void assertExceptionThrownWhenVerifyingMandatoryFields(ReturInfo returInfo, String fieldName) {
		try {
			returInfo.verifyMandatoryFields();
			fail();
		} catch (InvalidArgumentException e) {
			assertThat(e.getMessage(), containsString(fieldName));
		}
	}
	
}
