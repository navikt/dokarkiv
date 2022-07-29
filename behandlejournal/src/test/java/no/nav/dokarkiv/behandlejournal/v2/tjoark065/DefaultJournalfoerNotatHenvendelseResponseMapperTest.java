package no.nav.dokarkiv.behandlejournal.v2.tjoark065;

import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerNotatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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

	@BeforeEach
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