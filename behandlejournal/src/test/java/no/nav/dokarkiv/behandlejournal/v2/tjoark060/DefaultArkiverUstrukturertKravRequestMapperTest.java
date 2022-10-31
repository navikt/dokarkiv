package no.nav.dokarkiv.behandlejournal.v2.tjoark060;

import no.nav.dokarkiv.behandlejournal.SporingMapper;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.arkiverustrukturertkrav.Journalpost;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.ArkiverUstrukturertKravRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

	@BeforeEach
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
