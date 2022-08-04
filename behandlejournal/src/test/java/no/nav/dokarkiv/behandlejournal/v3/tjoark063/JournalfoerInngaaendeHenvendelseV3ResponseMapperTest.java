package no.nav.dokarkiv.behandlejournal.v3.tjoark063;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Test class for DefaultJournalfoerInngaaendeHenvendelseResponseMapper.
 * 
 * @author Rune Romundstad, Visma Consulting
 */
public class JournalfoerInngaaendeHenvendelseV3ResponseMapperTest {

	private static final Long JOURNALPOST_ID = 13L;

	private JournalfoerInngaaendeHenvendelseV3ResponseMapper mapper;
	private JournalfoerInngaaendeHenvendelseResponse domainResponse;
	private no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerInngaaendeHenvendelseResponse wsResponse;

	@BeforeEach
	public void setUp() {
		mapper = new JournalfoerInngaaendeHenvendelseV3ResponseMapper();
		domainResponse = new JournalfoerInngaaendeHenvendelseResponse(JOURNALPOST_ID);
	}

	@Test
	public void shouldMapFromDomainResponseToWsResponse() {
		wsResponse = mapper.map(domainResponse);
		assertThat(wsResponse.getJournalpostId(), is(JOURNALPOST_ID.toString()));
	}
}
