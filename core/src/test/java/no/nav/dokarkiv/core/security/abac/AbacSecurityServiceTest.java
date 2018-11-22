package no.nav.dokarkiv.core.security.abac;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_GSAK_SAKSID;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_PENSJON_SAKSID;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_PERSON_TILKNYTTET_FNR;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_TEMA;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.logging.AbacLogger;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositoryBegrenset;
import no.nav.freg.abac.core.annotation.context.AbacContext;
import no.nav.freg.abac.core.annotation.context.ThreadLocalAbacContext;
import no.nav.freg.abac.core.dto.request.XacmlAttribute;
import no.nav.freg.abac.core.dto.request.XacmlRequest;
import no.nav.freg.abac.core.dto.response.Advice;
import no.nav.freg.abac.core.dto.response.AttributeAssignment;
import no.nav.freg.abac.core.dto.response.Decision;
import no.nav.freg.abac.core.dto.response.Obligation;
import no.nav.freg.abac.core.dto.response.XacmlResponse;
import no.nav.freg.abac.core.service.AbacService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */

@RunWith(MockitoJUnitRunner.class)
public class AbacSecurityServiceTest {

	public static final Long DEFAULT_JOURNALPOST = 1L;
	private static final String SAK_ID = "123";

	private static AbacContext abacContext;

	@Mock
	private AbacLogger abaclog;
	@Mock
	private AbacService abacService;
	@Mock
	private JdbcAbacSecurityRepository jdbcAbacSecurityRepository;

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@InjectMocks
	private AbacSecurityService abacSecurityService;

	@Mock
	private JoarkRepository joarkRepository;

	@Mock
	private JoarkRepositoryBegrenset joarkRepositoryBegrenset;

	@Before
	public void setUp() throws Exception {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, Decision.PERMIT,
				Collections.<Obligation>emptyList(),
				Collections.<Advice>emptyList()));
		when(joarkRepositoryBegrenset.existsById(DEFAULT_JOURNALPOST)).thenReturn(true);
		abacContext = new ThreadLocalAbacContext();
		abacSecurityService.setAbacContext(abacContext);
	}

	@Test
	public void shouldCreateValidAbacRequest() throws Exception {
		AbacResources abacResources = new AbacResources();
		abacResources.setBrukerIds(Arrays.asList("2", "3"));
		when(jdbcAbacSecurityRepository.findAbacResources(DEFAULT_JOURNALPOST)).thenReturn(abacResources);

		abacSecurityService.assertAccessToJournalpost(String.valueOf(DEFAULT_JOURNALPOST));

		XacmlRequest request = getXacmlRequestFromAbacServiceMock();

		assertThat(request.getResources(), hasSize(0));
	}

	@Test
	public void shouldNotcallAbacLogMethod() throws Exception {
		AbacResources abacResources = new AbacResources();
		abacResources.setBrukerIds(Arrays.asList("2", "3"));
		when(jdbcAbacSecurityRepository.findAbacResources(DEFAULT_JOURNALPOST)).thenReturn(abacResources);

		abacSecurityService.assertAccessToJournalpost(String.valueOf(DEFAULT_JOURNALPOST));
		verify(abaclog, never()).logAbacDeny(any(XacmlRequest.class), any(XacmlResponse.class), anyMap());
		verify(abaclog, never()).logAbacPermit(any(XacmlRequest.class), any(XacmlResponse.class), anyMap());
	}

	@Test
	public void shouldIncludeResourceFellesTema() throws Exception {
		AbacResources abacResources = new AbacResources();
		abacResources.setFagomrade(FagomradeCode.FOR);

		XacmlRequest request = abacSecurityService.decorateJoarkResources(abacContext.getRequest(), abacResources, DEFAULT_JOURNALPOST);

		assertThat(request.getResources(), hasSize(1));
		assertThat(request.getResources().get(0), equalTo(new XacmlAttribute(RESOURCE_FELLES_TEMA, "FOR")));
		verify(abaclog, never()).logAbacDeny(any(XacmlRequest.class), any(XacmlResponse.class), anyMap());
	}

	@Test
	public void shouldThrowDecisionDenyException() throws Exception {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(
				new XacmlResponse(Decision.DENY, Decision.DENY,
						Collections.<Obligation>emptyList(),
						Collections.<Advice>emptyList()));

		AbacResources abacResources = new AbacResources();
		abacResources.setBrukerIds(Arrays.asList("2", "3"));
		when(jdbcAbacSecurityRepository.findAbacResources(DEFAULT_JOURNALPOST)).thenReturn(abacResources);

		try {
			abacSecurityService.assertAccessToJournalpost(String.valueOf(DEFAULT_JOURNALPOST));
			fail();
		} catch (AuthorizationException e) {
			assertThat(e.getMessage(), equalTo("Bruker har ikke tilgang til journalpost"));
		}
		verify(abaclog, times(1)).logAbacDeny(any(XacmlRequest.class), any(XacmlResponse.class), anyMap());
	}

	@Test
	public void shouldThrowJournalpostIkkeFunnetException() throws Exception {
		AbacResources abacResources = new AbacResources();
		abacResources.setBrukerIds(Arrays.asList("2", "3"));
		when(joarkRepositoryBegrenset.existsById(DEFAULT_JOURNALPOST)).thenReturn(false);

		try {
			abacSecurityService.assertAccessToJournalpost(String.valueOf(DEFAULT_JOURNALPOST));
			fail();
		} catch (JournalpostIkkeFunnetException e) {
			assertThat(e.getMessage(), equalTo("Journalpost ikke funnet. journalpostId=" + DEFAULT_JOURNALPOST));
		}
		verify(abaclog, never()).logAbacDeny(any(XacmlRequest.class), any(XacmlResponse.class), anyMap());
	}

	@Test
	public void shouldLogAndOnlyAcceptOneFNR() throws Exception {
		AbacResources abacResources = new AbacResources();
		abacResources.setBrukerIds(Arrays.asList("2"));
		when(jdbcAbacSecurityRepository.findAbacResources(DEFAULT_JOURNALPOST)).thenReturn(abacResources);

		abacSecurityService.assertAccessToJournalpost(String.valueOf(DEFAULT_JOURNALPOST));

		ArgumentCaptor<XacmlRequest> captor = ArgumentCaptor.forClass(XacmlRequest.class);
		verify(abacService).evaluate(captor.capture());
		XacmlRequest request = captor.getValue();

		assertThat((String) request.getResource().get(RESOURCE_FELLES_PERSON_TILKNYTTET_FNR).getValue(), equalTo("2"));
	}

	@Test
	public void shouldNotIncludeBrukerWhenMultipleBrukere() throws Exception {
		AbacResources abacResources = new AbacResources();
		abacResources.setBrukerIds(Arrays.asList("2", "3"));
		abacResources.setFagomrade(FagomradeCode.FOR);

		XacmlRequest request = abacSecurityService.decorateJoarkResources(abacContext.getRequest(), abacResources, DEFAULT_JOURNALPOST);
		assertThat(request.getResources(), hasSize(1));
	}

	@Test
	public void shouldCreateValidAbacRequestForPenSak() throws Exception {
		Decision decision = abacSecurityService.assertAccessToSak(SAK_ID, FagsystemCode.PEN);

		XacmlRequest request = getXacmlRequestFromAbacServiceMock();

		verify(abaclog, never()).logAbacDeny(any(XacmlRequest.class), any(XacmlResponse.class), anyMap());
		verify(abaclog, never()).logAbacPermit(any(XacmlRequest.class), any(XacmlResponse.class), anyMap());

		assertThat(decision, equalTo(Decision.PERMIT));
		assertThat(request.getResources(), hasSize(1));
		assertThat(request.getResources().get(0), equalTo(new XacmlAttribute(RESOURCE_ARKIV_PENSJON_SAKSID, SAK_ID)));
	}

	@Test
	public void shouldCreateValidAbacRequestForGsakSak() throws Exception {
		Decision decision = abacSecurityService.assertAccessToSak(SAK_ID, FagsystemCode.AO01);

		XacmlRequest request = getXacmlRequestFromAbacServiceMock();

		verify(abaclog, never()).logAbacDeny(any(XacmlRequest.class), any(XacmlResponse.class), anyMap());
		verify(abaclog, never()).logAbacPermit(any(XacmlRequest.class), any(XacmlResponse.class), anyMap());

		assertThat(decision, equalTo(Decision.PERMIT));
		assertThat(request.getResources(), hasSize(1));
		assertThat(request.getResources().get(0), equalTo(new XacmlAttribute(RESOURCE_ARKIV_GSAK_SAKSID, SAK_ID)));
	}

	@Test
	public void shouldReturnDenyForSak() throws Exception {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(
				new XacmlResponse(Decision.DENY, Decision.DENY,
						Collections.<Obligation>emptyList(),
						Collections.<Advice>emptyList()));

		Decision decision = abacSecurityService.assertAccessToSak(SAK_ID, FagsystemCode.AO01);

		verify(abaclog).logAbacDeny(any(XacmlRequest.class), any(XacmlResponse.class), anyMap());
		assertThat(decision, equalTo(Decision.DENY));
	}

	@Test
	public void shouldLogAdvice() throws Exception {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(
				new XacmlResponse(Decision.PERMIT, Decision.PERMIT,
						Collections.<Obligation>emptyList(),
						Arrays.asList(new Advice("id1", Collections.<AttributeAssignment>emptyList()),
								new Advice("id2", Collections.<AttributeAssignment>emptyList()))));

		Decision decision = abacSecurityService.assertAccessToSak(SAK_ID, FagsystemCode.AO01);

		verify(abaclog).logAbacPermit(any(XacmlRequest.class), any(XacmlResponse.class), anyMap());

		assertThat(decision, equalTo(Decision.PERMIT));
	}

	private XacmlRequest getXacmlRequestFromAbacServiceMock() {
		ArgumentCaptor<XacmlRequest> captor = ArgumentCaptor.forClass(XacmlRequest.class);
		verify(abacService, times(1)).evaluate(captor.capture());
		return captor.getValue();
	}
}
