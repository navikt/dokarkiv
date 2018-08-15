package no.nav.dokarkiv.behandlejournal.v3.tjoark061;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for DefaultLagreVedleggPaaJournalpostResponseMapper.
 * 
 * @author Rune Romundstad, Visma Consulting
 */
public class DefaultLagreVedleggPaaJournalpostV3ResponseMapperTest {
	
	private static final Long DOKUMENT_ID = 1L;
	
	private DefaultLagreVedleggPaaJournalpostV3ResponseMapper responseMapper;
	private LagreVedleggPaaJournalpostResponse domainResponse;
	
	@Before
	public void init() {
		domainResponse = new LagreVedleggPaaJournalpostResponse(DOKUMENT_ID);
		responseMapper = new DefaultLagreVedleggPaaJournalpostV3ResponseMapper();
	}
	
	@Test
	public void shouldMapDokumentIdFromDomainToWSResponse() {
		assertThat(responseMapper.map(domainResponse).getDokumentId(), is(String.valueOf(DOKUMENT_ID)));
	}
}
