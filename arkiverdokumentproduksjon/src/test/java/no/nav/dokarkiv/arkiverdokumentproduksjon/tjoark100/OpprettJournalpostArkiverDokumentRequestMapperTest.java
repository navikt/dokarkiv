package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.sporing.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokument.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test class for
 * DefaultOpprettJournalpostArkiverDokumentRequestMapper.
 *
 * @author Stig Strøm
 */
@RunWith(MockitoJUnitRunner.class)
public class OpprettJournalpostArkiverDokumentRequestMapperTest {

	@Mock
	private KildeNavnPopulator kildeNavnPopulator;

	@InjectMocks
	private OpprettJournalpostArkiverDokumentRequestMapper requestMapper = new OpprettJournalpostArkiverDokumentRequestMapper();

	private OpprettJournalpostArkiverDokumentRequest wsRequest;
	private OpprettJournalpostArkiverDokumentRequestTo domainRequest;
	private Journalpost inngaaendeWsJournalpost;

	@Before
	public void setUp() throws Exception {
		DateProvider.configure(true, "2014-08-27T12:00:00");
		createRequest();
		RequestContextSetter.setRequestContextForUnitTest();
	}

	@Test
	public void shouldMapOpprettOgFerdigstillRequestToTransferObject() throws Exception {
		domainRequest = requestMapper.map(wsRequest);
		no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost = domainRequest.getJournalpost();
		OpprettJournalpostArkiverDokumentAssertUtil.assertEqualJournalposts(domainJournalpost);
	}

	private void createRequest() throws Exception {
		inngaaendeWsJournalpost = OpprettJournalpostArkiverDokumentDataUtil.createJournalpost();
		wsRequest = new OpprettJournalpostArkiverDokumentRequest();
		wsRequest.setJournalpost(inngaaendeWsJournalpost);
	}


}