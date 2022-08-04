package no.nav.dokarkiv.core.domain.entities;

import no.nav.dokarkiv.core.domain.codes.ReferanseTypeCode;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.builder.KryssreferanseBuilder.getKryssreferanseBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for Kryssreferanse.
 */
public class KryssreferanseTest {

	@Test
	public void shouldThrowExceptionForMissingReferanseId() {
		Kryssreferanse kryssreferanse = getKryssreferanseBuilder()
				.referanseType(ReferanseTypeCode.SPOERSMAAL)
				.build();

		assertExceptionThrownWhenVerifyingMandatoryFields(kryssreferanse, "referanseId");
	}

	@Test
	public void shouldThrowExceptionForMissingReferanseType() {
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
