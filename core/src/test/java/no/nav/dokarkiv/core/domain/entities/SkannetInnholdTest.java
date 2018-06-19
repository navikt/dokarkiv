package no.nav.dokarkiv.core.domain.entities;

import static no.nav.dokarkiv.core.domain.builder.SkannetInnholdBuilder.getSkannetInnholdBuilder;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.junit.Test;

/**
 * Unit tests for SkannetInnhold.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class SkannetInnholdTest {

	@Test
	public void shouldThrowExceptionForMissingVedleggInnhold() throws Exception {
		SkannetInnhold skannetInnhold = getSkannetInnholdBuilder()
											.build();
		
		try {
			skannetInnhold.verifyMandatoryFields();
			fail();
		} catch (InvalidArgumentException e) {
			assertThat(e.getMessage(), containsString("vedleggInnhold"));
		}
	}
	
}
