package no.nav.dokarkiv.journal.v3;

import no.nav.tjeneste.virksomhet.journal.v3.JournalV3;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentResponse;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentURLRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentURLResponse;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.ws.WebServiceContext;
import java.security.Principal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JournalV3Endpoint.
 *
 * @author Stig Strøm, Acando
 */
@RunWith(MockitoJUnitRunner.class)
public class JournalV3EndpointTest {

	@Mock
	private JournalV3 journalV3ProviderMock;
	@Mock
	private WebServiceContext webServiceContextMock;
	@Mock
	private Principal principalMock;

	@InjectMocks
	private JournalV3Endpoint journalV3Endpoint;

	@Test
	public void shouldDelegateToProviderForHentKjerneJournalpostListe() throws Exception {
		HentKjerneJournalpostListeRequest request = new HentKjerneJournalpostListeRequest();
		HentKjerneJournalpostListeResponse response = new HentKjerneJournalpostListeResponse();
		when(journalV3ProviderMock.hentKjerneJournalpostListe(request)).thenReturn(response);

		HentKjerneJournalpostListeResponse wsResponse = journalV3Endpoint.hentKjerneJournalpostListe(request);

		assertThat(wsResponse, is(response));
	}

	@Test
	public void shouldDelegateToProviderForHentDokument() throws Exception {
		HentDokumentRequest request = new HentDokumentRequest();
		HentDokumentResponse response = new HentDokumentResponse();
		when(journalV3ProviderMock.hentDokument(request)).thenReturn(response);

		HentDokumentResponse wsResponse = journalV3Endpoint.hentDokument(request);

		assertThat(wsResponse, is(response));
	}

	@Test
	public void shouldDelegateToProviderForHentDokumentURL() throws Exception {
		HentDokumentURLRequest request = new HentDokumentURLRequest();
		HentDokumentURLResponse response = new HentDokumentURLResponse();
		when(journalV3ProviderMock.hentDokumentURL(request)).thenReturn(response);

		HentDokumentURLResponse wsResponse = journalV3Endpoint.hentDokumentURL(request);

		assertThat(wsResponse, is(response));
	}

	@Test
	public void shouldDelegateToProviderForPing() throws Exception {
		journalV3Endpoint.ping();

		verify(journalV3ProviderMock).ping();
	}


}