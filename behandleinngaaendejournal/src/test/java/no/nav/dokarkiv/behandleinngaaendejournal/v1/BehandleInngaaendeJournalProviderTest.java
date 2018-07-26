package no.nav.dokarkiv.behandleinngaaendejournal.v1;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.DokumentInfoIkkeTilknyttetJournalpostException;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.FerdigstillingIkkeMuligException;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.OppdaterJournalpostIkkeMuligException;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.UgyldigInputException;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.OppdaterJournalpostRequestMapper;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.OppdaterJournalpostService;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.OppdaterJournalpostRequestTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark067.FerdigstillJournalfoeringService;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark067.FerdigstillJournalfoeringTo;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeMidlertidigException;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringFerdigstillingIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringUgyldigInput;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostOppdateringIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostUgyldigInput;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.InngaaendeJournalpost;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.FerdigstillJournalfoeringRequest;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.OppdaterJournalpostRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Stig Strøm, Acando
 */
@RunWith(MockitoJUnitRunner.class)
public class BehandleInngaaendeJournalProviderTest {
	private static final String JOURNALPOST_ID = "42";
	private static final String ENHET_ID = "enhetID";

	@Rule
	public ExpectedException expected = ExpectedException.none();
	
	@Mock
	private FerdigstillJournalfoeringService ferdigstillJournalfoeringService;
	@Mock
	private OppdaterJournalpostService oppdaterJournalpostService;
	@Mock
	private OppdaterJournalpostRequestMapper oppdaterJournalpostRequestMapper;
	@Mock
	private AbacSecurityService abacService;

	@InjectMocks
	private BehandleInngaaendeJournalProvider provider;
	
	private OppdaterJournalpostRequest oppdaterJournalpostRequest = new OppdaterJournalpostRequest();
	private OppdaterJournalpostRequestTo oppdaterJournalpostTo = new OppdaterJournalpostRequestTo();

	private FerdigstillJournalfoeringRequest ferdigstillRequest;
	
	@Before
	public void setUp() {
		Mockito.when(oppdaterJournalpostRequestMapper.map(oppdaterJournalpostRequest)).thenReturn(oppdaterJournalpostTo);
		ferdigstillRequest = new FerdigstillJournalfoeringRequest();
		ferdigstillRequest.setJournalpostId(JOURNALPOST_ID);
		ferdigstillRequest.setEnhetId(ENHET_ID);

		InngaaendeJournalpost value = new InngaaendeJournalpost();
		value.setJournalpostId("1");
		oppdaterJournalpostRequest.setInngaaendeJournalpost(value);
	}
	
	@Test
	public void shouldOppdaterJournalpost() throws Exception {
		provider.oppdaterJournalpost(oppdaterJournalpostRequest);
		verify(oppdaterJournalpostRequestMapper).map(oppdaterJournalpostRequest);
		verify(oppdaterJournalpostService).oppdaterJournalpost(oppdaterJournalpostTo);
	}
	
	@Test
	public void shouldThrowOppdaterJournalpostUgyldigInputExceptionWhenIllegalArgumentException() throws Exception {
		expected.expect(OppdaterJournalpostUgyldigInput.class);
		doThrow(new IllegalArgumentException()).when(oppdaterJournalpostService).oppdaterJournalpost(oppdaterJournalpostTo);
		provider.oppdaterJournalpost(oppdaterJournalpostRequest);
	}
	
	@Test
	public void shouldThrowOppdaterJournalpostUgyldigInputExceptionWhenUgyldigInputException() throws Exception {
		expected.expect(OppdaterJournalpostUgyldigInput.class);
		doThrow(new UgyldigInputException()).when(oppdaterJournalpostService).oppdaterJournalpost(oppdaterJournalpostTo);
		provider.oppdaterJournalpost(oppdaterJournalpostRequest);
	}
	
	@Test
	public void shouldThrowOppdaterJournalpostOppdateringIkkeMuligWhenOppdaterJournalpostIkkeMuligException() throws Exception {
		expected.expect(OppdaterJournalpostOppdateringIkkeMulig.class);
		doThrow(new OppdaterJournalpostIkkeMuligException("test")).when(oppdaterJournalpostService).oppdaterJournalpost(oppdaterJournalpostTo);
		provider.oppdaterJournalpost(oppdaterJournalpostRequest);
	}
	
	@Test
	public void shouldThrowOppdaterJournalpostOppdateringIkkeMuligWhenJournalpostIkkeMidlertidigException() throws Exception {
		expected.expect(OppdaterJournalpostOppdateringIkkeMulig.class);
		doThrow(new JournalpostIkkeMidlertidigException("test")).when(oppdaterJournalpostService).oppdaterJournalpost(oppdaterJournalpostTo);
		provider.oppdaterJournalpost(oppdaterJournalpostRequest);
	}
	
	@Test
	public void shouldThrowOppdaterJournalpostObjektIkkeFunnetWhenJournalpostIkkeFunnetException() throws Exception {
		expected.expect(OppdaterJournalpostObjektIkkeFunnet.class);
		doThrow(new JournalpostIkkeFunnetException("test")).when(oppdaterJournalpostService).oppdaterJournalpost(oppdaterJournalpostTo);
		provider.oppdaterJournalpost(oppdaterJournalpostRequest);
	}
	
	@Test
	public void shouldThrowOppdaterJournalpostObjektIkkeFunnetWhenDokumentInfoIkkeTilknyttetJournalpostException() throws Exception {
		expected.expect(OppdaterJournalpostObjektIkkeFunnet.class);
		doThrow(new DokumentInfoIkkeTilknyttetJournalpostException("test")).when(oppdaterJournalpostService).oppdaterJournalpost(oppdaterJournalpostTo);
		provider.oppdaterJournalpost(oppdaterJournalpostRequest);
	}
	
	@Test
	public void shouldThrowOppdaterJournalpostJournalpostIkkeInngaaendeWhenJournalpostIkkeInngaaendeException() throws Exception {
		expected.expect(OppdaterJournalpostJournalpostIkkeInngaaende.class);
		doThrow(new JournalpostIkkeInngaaendeException()).when(oppdaterJournalpostService).oppdaterJournalpost(oppdaterJournalpostTo);
		provider.oppdaterJournalpost(oppdaterJournalpostRequest);
	}
	
	@Test
	public void shouldFerdigstillJournalpost() throws Exception {
		provider.ferdigstillJournalfoering(ferdigstillRequest);
		ArgumentCaptor<FerdigstillJournalfoeringTo> captor = ArgumentCaptor.forClass(FerdigstillJournalfoeringTo.class);
		verify(ferdigstillJournalfoeringService).ferdigstillJournalfoering(captor.capture());
		assertThat(captor.getValue().getJournalpostId(), is(equalTo(JOURNALPOST_ID)));
		assertThat(captor.getValue().getEnhetId(), is(equalTo(ENHET_ID)));
	}

	@Test
	public void shouldThrowFerdigstillJournalfoeringUgyldigInpuWhenInputIsNull() throws Exception {
		expected.expect(FerdigstillJournalfoeringUgyldigInput.class);
		provider.ferdigstillJournalfoering(null);
	}

	@Test
	public void shouldThrowFerdigstillJournalfoeringUgyldigInputWhenUgyldigInputException() throws Exception {
		expected.expect(FerdigstillJournalfoeringUgyldigInput.class);
		doThrow(new UgyldigInputException()).when(ferdigstillJournalfoeringService).ferdigstillJournalfoering(any(FerdigstillJournalfoeringTo.class));
		provider.ferdigstillJournalfoering(ferdigstillRequest);
	}
	
	@Test
	public void shouldThrowFerdigstillJournalfoeringFerdigstillingIkkeMuligWhenFerdigstillingIkkeMuligException() throws Exception {
		expected.expect(FerdigstillJournalfoeringFerdigstillingIkkeMulig.class);
		doThrow(new FerdigstillingIkkeMuligException("test")).when(ferdigstillJournalfoeringService).ferdigstillJournalfoering(any(FerdigstillJournalfoeringTo.class));
		provider.ferdigstillJournalfoering(ferdigstillRequest);
	}
	
	@Test
	public void shouldThrowFerdigstillJournalfoeringObjektIkkeFunnetWhenJournalpostIkkeFunnetException() throws Exception {
		expected.expect(FerdigstillJournalfoeringObjektIkkeFunnet.class);
		doThrow(new JournalpostIkkeFunnetException()).when(ferdigstillJournalfoeringService).ferdigstillJournalfoering(any(FerdigstillJournalfoeringTo.class));
		provider.ferdigstillJournalfoering(ferdigstillRequest);
	}
	
	@Test
	public void shouldThrowFerdigstillJournalfoeringJournalpostIkkeInngaaendeWhenJournalpostIkkeInngaaendeException() throws Exception {
		expected.expect(FerdigstillJournalfoeringJournalpostIkkeInngaaende.class);
		doThrow(new JournalpostIkkeInngaaendeException()).when(ferdigstillJournalfoeringService).ferdigstillJournalfoering(any(FerdigstillJournalfoeringTo.class));
		provider.ferdigstillJournalfoering(ferdigstillRequest);
	}
}
