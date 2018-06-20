package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusRequest;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for DefaultHentJournalOgDokumentStatusRequestMapper.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class DefaultHentJournalOgDokumentStatusRequestMapperTest {

	private static final Long JOURNALPOST_ID = 200L;
	private static final Long DOKUMENT_INFO_ID = 100L;

	private DefaultHentJournalOgDokumentStatusRequestMapper requestMapper;

	@Before
	public void setUp() throws Exception {
		requestMapper = new DefaultHentJournalOgDokumentStatusRequestMapper();
	}

	@Test
	public void shouldMapFromWsRequestToDomainRequest() {
		HentJournalOgDokumentStatusRequest wsRequest = createRequest();

		HentJournalOgDokumentStatusRequestTo domainRequest = requestMapper.map(wsRequest);

		assertThat(domainRequest.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(domainRequest.getDokumentInfoId(), is(DOKUMENT_INFO_ID));
	}

	private HentJournalOgDokumentStatusRequest createRequest() {
		HentJournalOgDokumentStatusRequest wsRequest = new HentJournalOgDokumentStatusRequest();
		wsRequest.setJournalpostId(JOURNALPOST_ID);
		wsRequest.setDokumentInfoId(DOKUMENT_INFO_ID);
		return wsRequest;
	}

}
