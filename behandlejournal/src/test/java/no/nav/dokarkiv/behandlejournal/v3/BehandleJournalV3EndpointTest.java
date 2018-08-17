package no.nav.dokarkiv.behandlejournal.v3;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.BehandleJournalV3;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.ArkiverUstrukturertKravRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.FerdigstillDokumentopplastingRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerInngaaendeHenvendelseRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerNotatRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerUtgaaendeHenvendelseRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.LagreVedleggPaaJournalpostRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.ws.WebServiceContext;
import java.security.Principal;

/**
 * Test class for the BehandleJournalEndpoint. To verify the ws operations call the
 * correct provider operation for further processing.
 *
 * @author Rune Romundstad, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class BehandleJournalV3EndpointTest {

	@Mock
	private BehandleJournalV3 behandleJournalProviderMock;
	@Mock
	private WebServiceContext webServiceContextMock;
	@Mock
	private Principal principalMock;
	@InjectMocks
	private BehandleJournalV3Endpoint behandleJournalEndpoint;

	@Before
	public void setUp() {
		when(webServiceContextMock.getUserPrincipal()).thenReturn(principalMock);
	}

	@Test
	public void shouldCallArkiverUstrukturertKravWithCorrectRequest() throws Exception {
		ArkiverUstrukturertKravRequest request = new ArkiverUstrukturertKravRequest();
		request.setApplikasjonsID("test");
		behandleJournalEndpoint.arkiverUstrukturertKrav(request);
		verify(behandleJournalProviderMock).arkiverUstrukturertKrav(request);
	}

	@Test
	public void shouldCallLagreVedleggPaaJournalpostWithCorrectRequest() throws Exception {
		LagreVedleggPaaJournalpostRequest request = new LagreVedleggPaaJournalpostRequest();
		request.setApplikasjonsID("test");
		behandleJournalEndpoint.lagreVedleggPaaJournalpost(request);
		verify(behandleJournalProviderMock).lagreVedleggPaaJournalpost(request);
	}

	@Test
	public void shouldCallFerdigstillDokumentopplastingWithCorrectRequest() throws Exception {
		FerdigstillDokumentopplastingRequest request = new FerdigstillDokumentopplastingRequest();
		request.setApplikasjonsID("test");
		behandleJournalEndpoint.ferdigstillDokumentopplasting(request);
		verify(behandleJournalProviderMock).ferdigstillDokumentopplasting(request);
	}

	@Test
	public void shouldCalljournalfoerUtgaaendeHenvendelseMedHoveddokumentWithCorrectRequest() throws Exception {
		JournalfoerUtgaaendeHenvendelseRequest request = new JournalfoerUtgaaendeHenvendelseRequest();
		request.setApplikasjonsID("test");
		behandleJournalEndpoint.journalfoerUtgaaendeHenvendelse(request);
		verify(behandleJournalProviderMock).journalfoerUtgaaendeHenvendelse(request);
	}

	@Test
	public void shouldCallJournalfoerNotatWithCorrectRequest() throws Exception {
		JournalfoerNotatRequest request = new JournalfoerNotatRequest();
		request.setApplikasjonsID("test");
		behandleJournalEndpoint.journalfoerNotat(request);
		verify(behandleJournalProviderMock).journalfoerNotat(request);
	}

	@Test
	public void shouldCalljournalfoerInngaaendeHenvendelseMedHoveddokumentWithCorrectRequest() throws Exception {
		JournalfoerInngaaendeHenvendelseRequest request =
				new JournalfoerInngaaendeHenvendelseRequest();
		request.setApplikasjonsID("test");
		behandleJournalEndpoint.journalfoerInngaaendeHenvendelse(request);
		verify(behandleJournalProviderMock).journalfoerInngaaendeHenvendelse(request);
	}
}
