package no.nav.dokarkiv.inngaaendejournal.v1;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.InngaaendeJournalV1;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostResponse;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * 
 * @author Stig Strøm, Acando
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class InngaaendeJournalEndpointTest {

	@Mock
	private InngaaendeJournalV1 providerMock;

	@InjectMocks
	private InngaaendeJournalEndpoint endpoint;

	@Test
	public void shouldDelegateToProviderForHentJournalpost() throws Exception {
		HentJournalpostRequest request = new HentJournalpostRequest();
		HentJournalpostResponse response = new HentJournalpostResponse();
		when(endpoint.hentJournalpost(request)).thenReturn(response);
		HentJournalpostResponse wsReponse = endpoint.hentJournalpost(request);
		assertThat(wsReponse, is(response));
	}

	@Test
	public void shouldDelegateToProviderForUtledJournalfoeringsbehov() throws Exception {
		UtledJournalfoeringsbehovRequest request = new UtledJournalfoeringsbehovRequest();
		UtledJournalfoeringsbehovResponse response = new UtledJournalfoeringsbehovResponse();
		when(endpoint.utledJournalfoeringsbehov(request)).thenReturn(response);
		UtledJournalfoeringsbehovResponse wsReponse = endpoint.utledJournalfoeringsbehov(request);
		assertThat(wsReponse, is(response));
	}

	@Test
	public void shouldDelegateToProviderForPing() throws Exception {
		endpoint.ping();

		verify(providerMock).ping();
	}

}
