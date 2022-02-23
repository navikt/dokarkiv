package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.ArkiverDokumentmottakV2;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.meldinger.JournalforInngaaendeForsendelseRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.meldinger.JournalforInngaaendeForsendelseResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.ws.WebServiceContext;
import java.security.Principal;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(MockitoJUnitRunner.class)
public class ArkiverDokumentmottakV2EndpointTest {

	@Mock
	private ArkiverDokumentmottakV2 arkiverDokumentmottakProviderMock;

	@Mock
	private WebServiceContext webServiceContextMock;

	@Mock
	private Principal principalMock;

	@InjectMocks
	private ArkiverDokumentmottakV2Endpoint endpoint;

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