package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@ExtendWith(MockitoExtension.class)
public class OpprettJournalpostArkiverDokumentResponseMapperTest {
	public static final Long JOURNALPOSTID = 112639812L;
	public static final Long DOKUMENTINFOID = 2348L;

	@InjectMocks
	private OpprettJournalpostArkiverDokumentResponseMapper responseMapper;
	private no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentResponse wsResponse;
	private OpprettJournalpostArkiverDokumentResponseTo domainResponse;

	@BeforeEach
	public void setUp() throws Exception {
		domainResponse = new OpprettJournalpostArkiverDokumentResponseTo(JOURNALPOSTID, DOKUMENTINFOID);
	}

	@Test
	public void shouldMapToOppdaterJournalpostOgFerdigstillJournalpostResponse() {
		wsResponse = responseMapper.map(domainResponse);
		assertThat(wsResponse.getJournalpostId(), is(JOURNALPOSTID));
		assertThat(wsResponse.getDokumentInfoId(), is(DOKUMENTINFOID));
	}
}