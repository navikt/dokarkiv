package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import no.nav.dokarkiv.core.sporing.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpost.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostAssertUtil.assertEqualJournalposts;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.PERSONIDENT;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.createJournalpost;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@ExtendWith(MockitoExtension.class)
public class OpprettJournalpostRequestMapperTest {

	@Mock
	private KildeNavnPopulator kildeNavnPopulator;

	@InjectMocks
	private OpprettJournalpostRequestMapper requestMapper;

	@BeforeEach
	public void setUp() throws Exception {
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