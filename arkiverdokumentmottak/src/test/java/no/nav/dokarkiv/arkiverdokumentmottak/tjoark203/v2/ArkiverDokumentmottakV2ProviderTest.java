package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.KanIkkeJournalfores;
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(MockitoJUnitRunner.class)
public class ArkiverDokumentmottakV2ProviderTest {


	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock
    private JoarkRepositorySkjermet joarkRepositoryMock;

	@Mock
	private JournalforInngaaendeForsendelseV2ResponseMapper journalforInngaaendeForsendelseV2ResponseMapperMock;

	@Mock
	private JournalforInngaaendeForsendelseV2RequestMapper journalforInngaaendeForsendelseV2RequestMapperMock;

	@Mock
	private JournalforInngaaendeForsendelseV2Service journalforInngaaendeForsendelseV2ServiceMock;

	@Mock
	private ArkiverDokumentmottakV2FaultInfoPopulator faultInfoPopulator;

	@InjectMocks
	private ArkiverDokumentmottakV2Provider provider;

	@Before
	public void setUp() {
		when(journalforInngaaendeForsendelseV2ServiceMock.journalforInngaaendeForsendelseV2(any()))
				.thenReturn(new JournalforInngaaendeForsendelseV2ResponseTo(1l));
		when(journalforInngaaendeForsendelseV2RequestMapperMock.map(any()))
				.thenReturn(new JournalforInngaaendeForsendelseV2RequestTo(true, new Journalpost()));

	}

	@Test
	public void shouldJournalforInngaaendeForsendelse() throws Exception {
		JournalforInngaaendeForsendelseResponse wsResponse =
				new JournalforInngaaendeForsendelseResponse();

		when(journalforInngaaendeForsendelseV2ResponseMapperMock
				.map(any(JournalforInngaaendeForsendelseV2ResponseTo.class)))
				.thenReturn(wsResponse);

		JournalforInngaaendeForsendelseResponse response = provider
				.journalforInngaaendeForsendelse(new JournalforInngaaendeForsendelseRequest());

		assertThat(response, is(wsResponse));
	}

	@Test
	public void journalforInngaaendeForsendelseThrowsKanIkkeJournalfores() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);

		when(journalforInngaaendeForsendelseV2ServiceMock
				.journalforInngaaendeForsendelseV2(any(JournalforInngaaendeForsendelseV2RequestTo.class)))
				.thenThrow(new IllegalArgumentException("test"));

		provider.journalforInngaaendeForsendelse(new JournalforInngaaendeForsendelseRequest());
	}

	@Test
	public void shouldPing() throws Exception {
		provider.ping();
	}
}