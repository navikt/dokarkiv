package no.nav.dokarkiv.behandlejournal.v2.tjoark064;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests for DefaultJournalfoerUtgaaendeHenvendelseResponseMapper
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 * 
 */
public class DefaultJournalfoerUtgaaendeHenvendelseResponseMapperTest {
	private static final Long JOURNALPOST_ID = 1L;

	private DefaultJournalfoerUtgaaendeHenvendelseResponseMapper mapper;
	private JournalfoerUtgaaendeHenvendelseResponse domainResponse;
	private no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerUtgaaendeHenvendelseResponse wsResponse;

	@BeforeEach
	public void setUp() {
		mapper = new DefaultJournalfoerUtgaaendeHenvendelseResponseMapper();
		domainResponse = new JournalfoerUtgaaendeHenvendelseResponse(JOURNALPOST_ID);
	}

	@Test
	public void shouldMapFromDomainResponseToWsResponse() {
		wsResponse = mapper.map(domainResponse);
		assertThat(wsResponse.getJournalpostId(), is(JOURNALPOST_ID.toString()));
	}
}
