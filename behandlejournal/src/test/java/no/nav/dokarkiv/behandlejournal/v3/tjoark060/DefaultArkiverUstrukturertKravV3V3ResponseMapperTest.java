package no.nav.dokarkiv.behandlejournal.v3.tjoark060;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for the ArkiverUstrukturertKravResponseMapper.
 * 
 * @author Rune Romundstad, Visma Consulting
 */
public class DefaultArkiverUstrukturertKravV3V3ResponseMapperTest {

	private static final Long JOURNALPOST_ID = 1L;
	private static final Long DOKUMENT_ID = 1L;
	
	private DefaultArkiverUstrukturertKravV3ResponseMapper responseMapper;
	private ArkiverUstrukturertKravResponse domainResponse;
	
	@Before
	public void init() {
		responseMapper = new DefaultArkiverUstrukturertKravV3ResponseMapper();
		domainResponse = new ArkiverUstrukturertKravResponse(JOURNALPOST_ID, DOKUMENT_ID);
	}
	
	@Test
	public void shouldMapJournalpostIdCorrectFromDomainResponseToWsResponse() {
		assertThat(responseMapper.map(domainResponse).getJournalpostId(), is(String.valueOf(JOURNALPOST_ID)));
	}
}
