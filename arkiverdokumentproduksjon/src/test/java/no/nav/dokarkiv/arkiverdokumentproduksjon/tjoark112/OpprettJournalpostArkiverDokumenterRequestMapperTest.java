package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;

import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.sporing.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.storage.BucketStorage;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumenterRequest;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterDataUtil.DOKUMENT_INNHOLD_BASE64;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterDataUtil.FILREFERANSE_GCS;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterDataUtil.PERSONIDENT;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for
 * DefaultOpprettJournalpostArkiverDokumentRequestMapper.
 *
 * @author Stig Strøm
 */
public class OpprettJournalpostArkiverDokumenterRequestMapperTest {

	private final KildeNavnPopulator kildeNavnPopulatorMock = mock(KildeNavnPopulator.class);
	private final BucketStorage storageMock = mock(BucketStorage.class);

	private final OpprettJournalpostArkiverDokumenterRequestMapper requestMapper = new OpprettJournalpostArkiverDokumenterRequestMapper(kildeNavnPopulatorMock, storageMock);

	@Before
	public void setUp() throws Exception {
		DateProvider.configure(true, "2014-08-27T12:00:00");
		createRequest();
		RequestContextSetter.setRequestContextForUnitTest();
		when(storageMock.downloadObject(eq(FILREFERANSE_GCS))).thenReturn(Optional.of("{\n" +
				"  \"axml\" : \"" + DOKUMENT_INNHOLD_BASE64 + "\",\n" +
				"  \"pdf\": \"" + DOKUMENT_INNHOLD_BASE64 + "\"\n" +
				"}"));
	}

	@Test
	public void shouldMapOpprettOgFerdigstillRequestToTransferObject() {
		OpprettJournalpostArkiverDokumenterRequestTo domainRequest = requestMapper.map(createRequest());
		no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost = domainRequest.getJournalpost();
		OpprettJournalpostArkiverDokumenterAssertUtil.assertEqualJournalposts(domainJournalpost);
	}

	@Test
	public void shouldTrimBrukerIdWhenBlankPadded() {
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().getBruker().setBrukerId("  " + PERSONIDENT);
		OpprettJournalpostArkiverDokumenterRequestTo domainRequest = requestMapper.map(request);
		no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost = domainRequest.getJournalpost();
		OpprettJournalpostArkiverDokumenterAssertUtil.assertEqualJournalposts(domainJournalpost);
	}

	private OpprettJournalpostArkiverDokumenterRequest createRequest() {
		Journalpost inngaaendeWsJournalpost = OpprettJournalpostArkiverDokumenterDataUtil.createJournalpost();
		OpprettJournalpostArkiverDokumenterRequest wsRequest = new OpprettJournalpostArkiverDokumenterRequest();
		wsRequest.setJournalpost(inngaaendeWsJournalpost);
		return wsRequest;
	}


}