package no.nav.dokarkiv.innsynjournal.v2.security.pip;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.innsynjournal.v2.security.XMLTypes;
import org.jboss.security.xacml.sunxacml.EvaluationCtx;
import org.jboss.security.xacml.sunxacml.attr.StringAttribute;
import org.jboss.security.xacml.sunxacml.cond.EvaluationResult;
import org.junit.Test;

/**
 * Unit tests for {@link AbstractJournalpostAttributeLocator}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class AbstractJournalpostAttributeLocatorTest {

	private static final String JOURNALPOST_ID = "1111";
	private EvaluationCtx contextMock = mock(EvaluationCtx.class);

	private AbstractJournalpostAttributeLocator locator = new AbstractJournalpostAttributeLocatorTester();

	@Test
	public void shouldFindJournalPostIdFromContext() throws Exception {
		mockJournalpostIdInContext();
		Long value = locator.getSubstituteValue(XMLTypes.STRING_TYPE_URI, contextMock);
		assertThat(value, is(Long.valueOf(JOURNALPOST_ID)));
	}

	private void mockJournalpostIdInContext() {
		when(contextMock.getResourceAttribute(XMLTypes.STRING_TYPE_URI,
				AbstractJournalpostAttributeLocator.JOURNALPOST_ID, null))
				.thenReturn(new EvaluationResult(new StringAttribute(JOURNALPOST_ID)));
	}

	private static class AbstractJournalpostAttributeLocatorTester extends AbstractJournalpostAttributeLocator {
	}
}