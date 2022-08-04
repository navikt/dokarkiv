package no.nav.dokarkiv.behandlejournal.v2.tjoark061;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Test class for DefaultLagreVedleggPaaJournalpostResponseMapper.
 * 
 * @author Rune Romundstad, Visma Consulting
 */
public class DefaultLagreVedleggPaaJournalpostResponseMapperTest {
	
	private static final Long DOKUMENT_ID = 1L;
	
	private DefaultLagreVedleggPaaJournalpostResponseMapper responseMapper;
	private LagreVedleggPaaJournalpostResponse domainResponse;

	@BeforeEach
	public void init() {
		domainResponse = new LagreVedleggPaaJournalpostResponse(DOKUMENT_ID);
		responseMapper = new DefaultLagreVedleggPaaJournalpostResponseMapper();
	}
	
	@Test
	public void shouldMapDokumentIdFromDomainToWSResponse() {
		assertThat(responseMapper.map(domainResponse).getDokumentId(), is(String.valueOf(DOKUMENT_ID)));
	}
}
