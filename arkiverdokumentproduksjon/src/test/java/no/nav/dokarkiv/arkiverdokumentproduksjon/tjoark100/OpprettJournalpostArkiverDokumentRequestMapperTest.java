package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.sporing.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokument.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100.OpprettJournalpostArkiverDokumentDataUtil.PERSONIDENT;

/**
 * Test class for
 * DefaultOpprettJournalpostArkiverDokumentRequestMapper.
 *
 * @author Stig Strøm
 */
@ExtendWith(MockitoExtension.class)
public class OpprettJournalpostArkiverDokumentRequestMapperTest {
	@Mock
	private KildeNavnPopulator kildeNavnPopulator;

	@InjectMocks
	private OpprettJournalpostArkiverDokumentRequestMapper requestMapper = new OpprettJournalpostArkiverDokumentRequestMapper();

	@BeforeEach
	public void setUp() throws Exception {
		DateProvider.configure(true, "2014-08-27T12:00:00");
		createRequest();
		RequestContextSetter.setRequestContextForUnitTest();
	}

	@Test
	public void shouldMapOpprettOgFerdigstillRequestToTransferObject() throws Exception {
		OpprettJournalpostArkiverDokumentRequest request = createRequest();
		OpprettJournalpostArkiverDokumentRequestTo domainRequest = requestMapper.map(request);
		no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost = domainRequest.getJournalpost();
		OpprettJournalpostArkiverDokumentAssertUtil.assertEqualJournalposts(domainJournalpost);
	}

	@Test
	public void shouldTrimBrukerIdWhenBlankPadded() throws Exception {
		OpprettJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpost().getBruker().setBrukerId("   " + PERSONIDENT);
		OpprettJournalpostArkiverDokumentRequestTo domainRequest = requestMapper.map(request);
		no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost = domainRequest.getJournalpost();
		OpprettJournalpostArkiverDokumentAssertUtil.assertEqualJournalposts(domainJournalpost);
	}

	private OpprettJournalpostArkiverDokumentRequest createRequest() throws Exception {
		Journalpost inngaaendeWsJournalpost = OpprettJournalpostArkiverDokumentDataUtil.createJournalpost();
		OpprettJournalpostArkiverDokumentRequest wsRequest = new OpprettJournalpostArkiverDokumentRequest();
		wsRequest.setJournalpost(inngaaendeWsJournalpost);
		return wsRequest;
	}


}