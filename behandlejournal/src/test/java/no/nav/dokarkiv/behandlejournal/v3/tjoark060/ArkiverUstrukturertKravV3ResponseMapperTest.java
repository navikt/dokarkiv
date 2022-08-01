package no.nav.dokarkiv.behandlejournal.v3.tjoark060;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Test class for the ArkiverUstrukturertKravResponseMapper.
 * 
 * @author Rune Romundstad, Visma Consulting
 */
public class ArkiverUstrukturertKravV3ResponseMapperTest {

	private static final Long JOURNALPOST_ID = 1L;
	private static final Long DOKUMENT_ID = 1L;
	
	private ArkiverUstrukturertKravV3ResponseMapper responseMapper;
	private ArkiverUstrukturertKravResponse domainResponse;

	@BeforeEach
	public void init() {
		responseMapper = new ArkiverUstrukturertKravV3ResponseMapper();
		domainResponse = new ArkiverUstrukturertKravResponse(JOURNALPOST_ID, DOKUMENT_ID);
	}
	
	@Test
	public void shouldMapJournalpostIdCorrectFromDomainResponseToWsResponse() {
		assertThat(responseMapper.map(domainResponse).getJournalpostId(), is(String.valueOf(JOURNALPOST_ID)));
	}
}
