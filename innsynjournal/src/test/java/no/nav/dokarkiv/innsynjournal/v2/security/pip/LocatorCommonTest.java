package no.nav.dokarkiv.innsynjournal.v2.security.pip;


import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.innsynjournal.v2.security.XMLTypes;
import org.jboss.security.xacml.sunxacml.cond.EvaluationResult;
import org.junit.Test;

import java.net.URI;

/**
 * Unit test class for {@link LocatorCommon}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class LocatorCommonTest {

	private static final String ATTRIBUTE_ID = "id";

	@Test
	public void shouldCreateEmptyBag() throws Exception {
		EvaluationResult result = LocatorCommon.createEmptyEvaluationResult(new URI(ATTRIBUTE_ID), XMLTypes.STRING_TYPE_URI);

		assertThat(result.getAttributeValue().getValue(), is(nullValue()));
		assertThat(result.getAttributeValue().getType(), is(XMLTypes.STRING_TYPE_URI));
	}

	@Test
	public void shouldCreateEmptyBagWhenTypeIsNull() throws Exception {
		URI id = new URI(ATTRIBUTE_ID);
		EvaluationResult result = LocatorCommon.createEmptyEvaluationResult(id, null);

		assertThat(result.getAttributeValue().getValue(), is(nullValue()));
		assertThat(result.getAttributeValue().getType(), is(id));
	}
}