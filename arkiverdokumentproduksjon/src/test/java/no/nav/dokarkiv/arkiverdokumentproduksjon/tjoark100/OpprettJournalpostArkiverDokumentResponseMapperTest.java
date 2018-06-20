package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@RunWith(MockitoJUnitRunner.class)
public class OpprettJournalpostArkiverDokumentResponseMapperTest {
	public static final Long JOURNALPOSTID = 112639812L;
	public static final Long DOKUMENTINFOID = 2348L;

	@InjectMocks
	private OpprettJournalpostArkiverDokumentResponseMapper responseMapper;
	private no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentResponse wsResponse;
	private OpprettJournalpostArkiverDokumentResponseTo domainResponse;

	@Before
	public void setUp() throws Exception {
		domainResponse = new OpprettJournalpostArkiverDokumentResponseTo(JOURNALPOSTID, DOKUMENTINFOID);
	}

	@Test
	public void shouldMapToOppdaterJournalpostOgFerdigstillJournalpostResponse() throws Exception {
		wsResponse = responseMapper.map(domainResponse);
		assertThat(wsResponse.getJournalpostId(), is(JOURNALPOSTID));
		assertThat(wsResponse.getDokumentInfoId(), is(DOKUMENTINFOID));
	}
}