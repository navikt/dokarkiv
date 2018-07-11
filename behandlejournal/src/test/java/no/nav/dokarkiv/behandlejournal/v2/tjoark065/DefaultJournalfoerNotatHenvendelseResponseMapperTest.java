package no.nav.dokarkiv.behandlejournal.v2.tjoark065;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerNotatResponse;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for
 * {@link DefaultJournalfoerNotatHenvendelseResponseMapper}
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class DefaultJournalfoerNotatHenvendelseResponseMapperTest {
	private static final Long JOURNALPOST_ID = 1L;

	private DefaultJournalfoerNotatHenvendelseResponseMapper mapper;
	private JournalfoerNotatHenvendelseResponse domainResponse;
	private JournalfoerNotatResponse wsResponse;

	@Before
	public void setUp() {
		mapper = new DefaultJournalfoerNotatHenvendelseResponseMapper();
		domainResponse = new JournalfoerNotatHenvendelseResponse(JOURNALPOST_ID);
	}

	@Test
	public void shouldMapFromDomainResponseToWsResponse() {
		wsResponse = mapper.map(domainResponse);
		assertThat(wsResponse.getJournalpostId(), is(JOURNALPOST_ID.toString()));
	}
}