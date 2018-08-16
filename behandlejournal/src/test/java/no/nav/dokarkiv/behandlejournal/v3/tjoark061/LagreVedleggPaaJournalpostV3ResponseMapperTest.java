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
public class LagreVedleggPaaJournalpostV3ResponseMapperTest {
	
	private static final Long DOKUMENT_ID = 1L;
	
	private LagreVedleggPaaJournalpostV3ResponseMapper responseMapper;
	private LagreVedleggPaaJournalpostResponse domainResponse;
	
	@Before
	public void init() {
		domainResponse = new LagreVedleggPaaJournalpostResponse(DOKUMENT_ID);
		responseMapper = new LagreVedleggPaaJournalpostV3ResponseMapper();
	}
	
	@Test
	public void shouldMapDokumentIdFromDomainToWSResponse() {
		assertThat(responseMapper.map(domainResponse).getDokumentId(), is(String.valueOf(DOKUMENT_ID)));
	}
}
