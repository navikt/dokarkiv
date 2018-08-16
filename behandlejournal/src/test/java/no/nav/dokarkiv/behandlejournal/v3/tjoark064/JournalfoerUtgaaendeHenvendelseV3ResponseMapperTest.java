package no.nav.dokarkiv.behandlejournal.v3.tjoark064;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for DefaultJournalfoerUtgaaendeHenvendelseResponseMapper
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 * 
 */
public class JournalfoerUtgaaendeHenvendelseV3ResponseMapperTest {
	private static final Long JOURNALPOST_ID = 1L;

	private JournalfoerUtgaaendeHenvendelseV3ResponseMapper mapper;
	private JournalfoerUtgaaendeHenvendelseResponse domainResponse;
	private no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerUtgaaendeHenvendelseResponse wsResponse;

	@Before
	public void setUp() {
		mapper = new JournalfoerUtgaaendeHenvendelseV3ResponseMapper();
		domainResponse = new JournalfoerUtgaaendeHenvendelseResponse(JOURNALPOST_ID);
	}

	@Test
	public void shouldMapFromDomainResponseToWsResponse() {
		wsResponse = mapper.map(domainResponse);
		assertThat(wsResponse.getJournalpostId(), is(JOURNALPOST_ID.toString()));
	}
}
