package no.nav.dokarkiv.core.domain.entities;

import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.builder.SkannetInnholdBuilder.getSkannetInnholdBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for SkannetInnhold.
 */
public class SkannetInnholdTest {

	@Test
	public void shouldThrowExceptionForMissingVedleggInnhold() {
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
