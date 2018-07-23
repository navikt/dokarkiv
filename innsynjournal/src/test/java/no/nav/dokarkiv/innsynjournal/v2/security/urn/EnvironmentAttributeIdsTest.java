package no.nav.dokarkiv.innsynjournal.v2.security.urn;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Unit test for {@link EnvironmentAttributeIds}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class EnvironmentAttributeIdsTest {

	private static final String ENVIRONMENT_RECIEVER = "urn:nav:ikt:tilgangskontroll:xacml:joark:environment:environment-receiver";

	@Test
	public void shouldReturnEnvironmentReciever() throws Exception {
		assertThat(EnvironmentAttributeIds.ATTR_ENVIRONMENT_RECIEVER, is(ENVIRONMENT_RECIEVER));
		assertThat(EnvironmentAttributeIds.ENVIRONMENT_RECIEVER.getURN(), is(ENVIRONMENT_RECIEVER));
	}

}