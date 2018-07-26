package no.nav.dokarkiv.behandleinngaaendejournal.v1;

import static org.mockito.Mockito.verify;

import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.FerdigstillJournalfoeringRequest;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.OppdaterJournalpostRequest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for BehandleInngaaendeJournalEndpointTest
 *
 * @author Stig Strøm, Acando
 */
@RunWith(MockitoJUnitRunner.class)
public class BehandleInngaaendeJournalEndpointTest {

	@Mock
	private BehandleInngaaendeJournalV1 provider;

	@InjectMocks
	private BehandleInngaaendeJournalEndpoint endpoint;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void shouldDelegateToProviderForFerdigstillJournalfoering() throws Exception {
		FerdigstillJournalfoeringRequest request = new FerdigstillJournalfoeringRequest();
		endpoint.ferdigstillJournalfoering(request);
		verify(provider).ferdigstillJournalfoering(request); 
	}
	
	@Test
	public void shouldDelegateToProviderForOppdaterJournalfoering() throws Exception {
		OppdaterJournalpostRequest request = new OppdaterJournalpostRequest();
		endpoint.oppdaterJournalpost(request);
		verify(provider).oppdaterJournalpost(request); 
	}

	@Test
	public void shouldDelegateToProviderForPing() throws Exception {
		endpoint.ping();

		verify(provider).ping();
	}
}