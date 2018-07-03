package no.nav.dokarkiv.arkiverdokumentmottak.v1;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1.ArkiverDokumentmottakEndpoint;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.ArkiverDokumentmottakV1;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.ws.WebServiceContext;
import java.security.Principal;

/**
 * Unit tests for ArkiverDokumentmottakEndpoint
 *
 * @author Stig Strøm, Acando
 */
@RunWith(MockitoJUnitRunner.class)
public class ArkiverDokumentmottakEndpointTest {

	@Mock
	private ArkiverDokumentmottakV1 arkiverDokumentmottakProviderMock;

	@Mock
	private WebServiceContext webServiceContextMock;

	@Mock
	private Principal principalMock;

	@InjectMocks
	private ArkiverDokumentmottakEndpoint endpoint;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() {
		when(webServiceContextMock.getUserPrincipal()).thenReturn(principalMock);
	}

	@Test
	public void shouldDelegateToProviderForJournalforInngaaendeForsendelse() throws Exception {
		JournalforInngaaendeForsendelseRequest request = new JournalforInngaaendeForsendelseRequest();
		JournalforInngaaendeForsendelseResponse response = new JournalforInngaaendeForsendelseResponse();
		when(arkiverDokumentmottakProviderMock.journalforInngaaendeForsendelse(request)).thenReturn(response);

		JournalforInngaaendeForsendelseResponse wsReponse = endpoint.journalforInngaaendeForsendelse(request);
		assertThat(wsReponse, is(response));
    }

	@Test
	public void shouldDelegateToProviderForPing() throws Exception {
		endpoint.ping();

		verify(arkiverDokumentmottakProviderMock).ping();
	}
}