package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostAssertUtil.assertEqualJournalposts;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.PERSONIDENT;
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

	@Before
	public void setUp() throws Exception {
		DateProvider.configure(true, "2014-08-27T12:00:00");
		createRequest();
		RequestContextSetter.setRequestContextForUnitTest();
	}

	@Test
	public void shouldMapOpprettJournalpostRequestToTransferObject() throws Exception {
		OpprettJournalpostRequest wsRequest = createRequest();
		OpprettJournalpostRequestTo domainRequest = requestMapper.map(wsRequest);
		assertEqualJournalposts(domainRequest.getJournalpost());
	}

	@Test
	public void shouldTrimBrukerIdWhenBlankPadded() throws Exception {
		OpprettJournalpostRequest wsRequest = createRequest();
		wsRequest.getJournalpost().getBruker().setBrukerId("   " + PERSONIDENT);
		OpprettJournalpostRequestTo domainRequest = requestMapper.map(wsRequest);
		assertEqualJournalposts(domainRequest.getJournalpost());
	}

	private OpprettJournalpostRequest createRequest() throws Exception {
		Journalpost inngaaendeWsJournalpost = createJournalpost();
		OpprettJournalpostRequest wsRequest = new OpprettJournalpostRequest();
		wsRequest.setJournalpost(inngaaendeWsJournalpost);
		return wsRequest;
	}

}