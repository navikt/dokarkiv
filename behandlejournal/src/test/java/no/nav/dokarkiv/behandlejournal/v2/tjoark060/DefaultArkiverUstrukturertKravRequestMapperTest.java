package no.nav.dokarkiv.behandlejournal.v2.tjoark060;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.behandlejournal.SporingMapper;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.arkiverustrukturertkrav.Journalpost;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.ArkiverUstrukturertKravRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test class for the ArkiverUstrukturertKravRequestMapper.
 *
 * @author Rune Romundstad, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultArkiverUstrukturertKravRequestMapperTest {
	private static final String SPORING_FORNAVN = "fornavn";
	private static final String SPORING_ETTERNAVN = "etternavn";

	@Mock
	private JournalpostMapper journalpostMapperMock;
	@Mock
	private SporingMapper sporingMapperMock;
	@InjectMocks
	private DefaultArkiverUstrukturertKravRequestMapper requestMapper;

	private ArkiverUstrukturertKravRequest wsRequest;
	private no.nav.dokarkiv.behandlejournal.v2.tjoark060.ArkiverUstrukturertKravRequest domainRequest;

	@Before
	public void setUp() {
		createRequest();
	}

	private void createRequest() {
		wsRequest = new ArkiverUstrukturertKravRequest();
		wsRequest.setPersonFornavn(SPORING_FORNAVN);
		wsRequest.setPersonEtternavn(SPORING_ETTERNAVN);
		wsRequest.setApplikasjonsID("applikasjonsid");
	}

	@Test
	public void shouldMapJournalpostFromWsRequestToDomainRequest() {
		Journalpost wsJournalpost = new Journalpost();
		wsRequest.setJournalpost(wsJournalpost);

		no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost = new no.nav.dokarkiv.core.domain.entities.Journalpost();
		when(journalpostMapperMock.map(wsJournalpost)).thenReturn(domainJournalpost);

		domainRequest = requestMapper.map(wsRequest);

		assertThat(domainRequest.getJournalpost(), is(domainJournalpost));
		verify(sporingMapperMock).mapSporingsinfo(domainJournalpost, SPORING_FORNAVN + " " + SPORING_ETTERNAVN);
	}
}
