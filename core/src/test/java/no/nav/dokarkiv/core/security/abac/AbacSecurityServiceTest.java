package no.nav.dokarkiv.core.security.abac;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.logging.AbacLogger;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_PENSJON_SAKSID;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_PERSON_TILKNYTTET_FNR;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_TEMA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */

@ExtendWith(MockitoExtension.class)
public class AbacSecurityServiceTest {

	public static final Long DEFAULT_JOURNALPOST = 1L;
	private static final String SAK_ID = "123";

	@Mock
	private static AbacContext abacContext;
	@Mock
	private AbacLogger abaclog;
	@Mock
	private AbacService abacService;
	@Mock
	private JdbcAbacSecurityRepository jdbcAbacSecurityRepository;
	@InjectMocks
	private AbacSecurityService abacSecurityService;
	@Mock
	private JoarkRepositorySkjermet joarkRepositorySkjermet;

	@BeforeEach
	public void setUp() throws Exception {
		lenient().when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, Decision.PERMIT,
				Collections.<Obligation>emptyList(),
				Collections.<Advice>emptyList()));
		lenient().when(joarkRepositorySkjermet.existsById(DEFAULT_JOURNALPOST)).thenReturn(true);
		lenient().when(abacContext.getRequest()).thenReturn(new ThreadLocalAbacContext().getRequest());
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
		when(joarkRepositorySkjermet.existsById(DEFAULT_JOURNALPOST)).thenReturn(false);

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
	public void shouldReturnDenyForSak() throws Exception {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(
				new XacmlResponse(Decision.DENY, Decision.DENY,
						Collections.<Obligation>emptyList(),
						Collections.<Advice>emptyList()));

		Decision decision = abacSecurityService.assertAccessToSak(SAK_ID, FagsystemCode.FS22);

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

		Decision decision = abacSecurityService.assertAccessToSak(SAK_ID, FagsystemCode.FS22);

		verify(abaclog).logAbacPermit(any(XacmlRequest.class), any(XacmlResponse.class), anyMap());

		assertThat(decision, equalTo(Decision.PERMIT));
	}

	private XacmlRequest getXacmlRequestFromAbacServiceMock() {
		ArgumentCaptor<XacmlRequest> captor = ArgumentCaptor.forClass(XacmlRequest.class);
		verify(abacService, times(1)).evaluate(captor.capture());
		return captor.getValue();
	}
}
