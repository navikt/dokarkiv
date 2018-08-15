package no.nav.dokarkiv.behandlejournal.v3.tjoark063;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for DefaultJournalfoerInngaaendeHenvendelseResponseMapper.
 * 
 * @author Rune Romundstad, Visma Consulting
 */
public class DefaultJournalfoerInngaaendeHenvendelseResponseMapperTest {

	private static final Long JOURNALPOST_ID = 13L;

	private DefaultJournalfoerInngaaendeHenvendelseResponseMapper mapper;
	private JournalfoerInngaaendeHenvendelseResponse domainResponse;
	private no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerInngaaendeHenvendelseResponse wsResponse;

	@Before
	public void setUp() {
		mapper = new DefaultJournalfoerInngaaendeHenvendelseResponseMapper();
		domainResponse = new JournalfoerInngaaendeHenvendelseResponse(JOURNALPOST_ID);
	}

	@Test
	public void shouldMapFromDomainResponseToWsResponse() {
		wsResponse = mapper.map(domainResponse);
		assertThat(wsResponse.getJournalpostId(), is(JOURNALPOST_ID.toString()));
	}
}
