package no.nav.dokarkiv.innsynjournal.v2.security.pip;

import no.nav.dokarkiv.innsynjournal.v2.security.XMLTypes;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jboss.security.xacml.sunxacml.EvaluationCtx;
import org.jboss.security.xacml.sunxacml.attr.StringAttribute;
import org.jboss.security.xacml.sunxacml.cond.EvaluationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import java.net.URI;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JournalpostAvsenderLocator}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
@RunWith(MockitoJUnitRunner.class)
public class JournalpostAvsenderLocatorTest {

	private static final String JOURNALPOST_ID = "1";
	private static final String AVSENDER_FNR = "01010124789";
	private URI attributeId = URI.create("urn:nav:ikt:tilgangskontroll:xacml:resource:attr:journalpost:avsenderfnr");
	private int designatorType = 1;
	private EvaluationCtx contextMock = mock(EvaluationCtx.class);

	@InjectMocks
	private JournalpostAvsenderLocator journalpostAvsenderLocator = new JournalpostAvsenderLocator();

	@Mock
	private EntityManager entityManager;

	@Before
	public void setUp() {
		journalpostAvsenderLocator.getSupportedIds().add(attributeId);
		mockJournalpostIdInContext();
	}

	@Test
	public void shouldReturnEmptyBagForUnsupportedId() throws Exception {
		EvaluationResult result = journalpostAvsenderLocator
				.findAttribute(XMLTypes.STRING_TYPE_URI, URI.create("notSupported"),
						null, null, contextMock, designatorType);

		verify(entityManager, never()).unwrap(Session.class);
		assertThat(result.getAttributeValue().getValue(), is(nullValue()));
		assertThat(result.getAttributeValue().getType(), is(XMLTypes.STRING_TYPE_URI));
	}

	@Test
	public void shouldReturnEmptyBagWhenTypeIsNull() throws Exception {
		URI notSupported = URI.create("notSupported");
		EvaluationResult result = journalpostAvsenderLocator
				.findAttribute(null, notSupported,
						null, null, contextMock, designatorType);

		verify(entityManager, never()).unwrap(Session.class);
		assertThat(result.getAttributeValue().getValue(), is(nullValue()));
		assertThat(result.getAttributeValue().getType(), is(notSupported));
	}

	@Test
	public void shouldFindJournalPostIdFromContext() throws Exception {
		mockJournalpostIdInContext();
		Long value = journalpostAvsenderLocator.getSubstituteValue(XMLTypes.STRING_TYPE_URI, contextMock);
		assertThat(value, is(Long.valueOf(JOURNALPOST_ID)));
	}

	@Test
	public void shouldCreateCorrectAvsenderQuery() throws Exception {
		Session session = mockCurrentSession();
		mockQuery(session, AVSENDER_FNR);

		runFindAttribute();

		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(session).createQuery(captor.capture());
		assertThat(captor.getValue(), is("select j.avsenderMottakerId from Journalpost j where j.journalpostId = :journalpostId"));
	}

	@Test
	public void shouldQueryDataBaseWithCorrectJournalpostId() throws Exception {
		Session session = mockCurrentSession();
		Query queryMock = mockQuery(session, AVSENDER_FNR);

		runFindAttribute();

		verify(queryMock).setParameter("journalpostId", Long.valueOf(JOURNALPOST_ID));
	}

	@Test
	public void shouldReturnAvsenderFnr() throws Exception {
		mockAvsender(AVSENDER_FNR);

		EvaluationResult attribute = runFindAttribute();

		assertThat((String) attribute.getAttributeValue().getValue(), is(AVSENDER_FNR));
	}

	@Test
	public void shouldReturnEmptyBagIfAvsenderFnrIsNull() throws Exception {
		mockAvsender(null);

		EvaluationResult result = runFindAttribute();

		assertThat(result.getAttributeValue().getValue(), is(nullValue()));
		assertThat(result.getAttributeValue().getType(), is(XMLTypes.STRING_TYPE_URI));
	}

	private EvaluationResult runFindAttribute() {
		return journalpostAvsenderLocator.findAttribute(XMLTypes.STRING_TYPE_URI, attributeId,
				null, null, contextMock, designatorType);
	}

	private void mockJournalpostIdInContext() {
		when(contextMock.getResourceAttribute(XMLTypes.STRING_TYPE_URI,
				AbstractJournalpostAttributeLocator.JOURNALPOST_ID, null))
				.thenReturn(new EvaluationResult(new StringAttribute(JOURNALPOST_ID)));
	}

	private void mockAvsender(String avsender) {
		Session session = mockCurrentSession();
		mockQuery(session, avsender);
	}

	private Session mockCurrentSession() {
		Session session = mock(Session.class);
		when(entityManager.unwrap(Session.class)).thenReturn(session);
		return session;
	}

	private Query mockQuery(Session session, String avsender) {
		Query queryMock = mock(Query.class);
		when(session.createQuery(anyString())).thenReturn(queryMock);
		when(queryMock.setParameter(anyString(), anyLong())).thenReturn(queryMock);
		when(queryMock.uniqueResult()).thenReturn(avsender);
		return queryMock;
	}


}