package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostAssertUtil.assertEqualJournalposts;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.createJournalpost;

import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.sporing.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpost.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@RunWith(MockitoJUnitRunner.class)
public class OpprettJournalpostRequestMapperTest {

	@Mock
	private KildeNavnPopulator kildeNavnPopulator;

	@InjectMocks
	private OpprettJournalpostRequestMapper requestMapper;

	private OpprettJournalpostRequest wsRequest;
	private OpprettJournalpostRequestTo domainRequest;
	private Journalpost inngaaendeWsJournalpost;

	@Before
	public void setUp() throws Exception {
		DateProvider.configure(true, "2014-08-27T12:00:00");
		createRequest();
		RequestContextSetter.setRequestContextForUnitTest();
	}

	@Test
	public void shouldMapOpprettJournalpostRequestToTransferObject() throws Exception {
		domainRequest = requestMapper.map(wsRequest);
		assertEqualJournalposts(domainRequest.getJournalpost());
	}

	private void createRequest() throws Exception {
		inngaaendeWsJournalpost = createJournalpost();
		wsRequest = new OpprettJournalpostRequest();
		wsRequest.setJournalpost(inngaaendeWsJournalpost);
	}

}