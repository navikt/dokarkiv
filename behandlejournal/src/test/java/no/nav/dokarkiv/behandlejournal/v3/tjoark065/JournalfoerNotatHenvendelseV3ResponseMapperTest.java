package no.nav.dokarkiv.behandlejournal.v3.tjoark065;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerNotatResponse;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for
 * {@link JournalfoerNotatHenvendelseV3ResponseMapper}
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class JournalfoerNotatHenvendelseV3ResponseMapperTest {
	private static final Long JOURNALPOST_ID = 1L;

	private JournalfoerNotatHenvendelseV3ResponseMapper mapper;
	private JournalfoerNotatHenvendelseResponse domainResponse;
	private JournalfoerNotatResponse wsResponse;

	@Before
	public void setUp() {
		mapper = new JournalfoerNotatHenvendelseV3ResponseMapper();
		domainResponse = new JournalfoerNotatHenvendelseResponse(JOURNALPOST_ID);
	}

	@Test
	public void shouldMapFromDomainResponseToWsResponse() {
		wsResponse = mapper.map(domainResponse);
		assertThat(wsResponse.getJournalpostId(), is(JOURNALPOST_ID.toString()));
	}
}