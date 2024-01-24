package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;

import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.sporing.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.storage.BucketStorage;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumenterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterAssertUtil.assertEqualJournalposts;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterDataUtil.DOKUMENT_INNHOLD_BASE64;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterDataUtil.FILREFERANSE_GCS;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterDataUtil.PERSONIDENT;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterDataUtil.createJournalpost;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for
 * DefaultOpprettJournalpostArkiverDokumentRequestMapper.
 */
public class OpprettJournalpostArkiverDokumenterRequestMapperTest {

	private final KildeNavnPopulator kildeNavnPopulatorMock = mock(KildeNavnPopulator.class);
	private final BucketStorage storageMock = mock(BucketStorage.class);

	private final OpprettJournalpostArkiverDokumenterRequestMapper requestMapper = new OpprettJournalpostArkiverDokumenterRequestMapper(kildeNavnPopulatorMock, storageMock);

	@BeforeEach
	public void setUp() throws Exception {
		DateProvider.configure(true, "2014-08-27T12:00:00");
		createRequest();
		RequestContextSetter.setRequestContextForUnitTest();
		when(storageMock.downloadObject(eq(FILREFERANSE_GCS), anyString())).thenReturn(Optional.of("""
				{
				  "axml" : "%s",
				  "pdf": "%s"
				}
				""".formatted(DOKUMENT_INNHOLD_BASE64, DOKUMENT_INNHOLD_BASE64)));
	}

	@Test
	public void shouldMapOpprettOgFerdigstillRequestToTransferObject() {
		OpprettJournalpostArkiverDokumenterRequestTo domainRequest = requestMapper.map(createRequest());
		no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost = domainRequest.getJournalpost();
		assertEqualJournalposts(domainJournalpost);
	}

	@Test
	public void shouldTrimBrukerIdWhenBlankPadded() {
		OpprettJournalpostArkiverDokumenterRequest request = createRequest();
		request.getJournalpost().getBruker().setBrukerId("  " + PERSONIDENT);
		OpprettJournalpostArkiverDokumenterRequestTo domainRequest = requestMapper.map(request);
		no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost = domainRequest.getJournalpost();
		assertEqualJournalposts(domainJournalpost);
	}

	private OpprettJournalpostArkiverDokumenterRequest createRequest() {
		Journalpost inngaaendeWsJournalpost = createJournalpost();
		OpprettJournalpostArkiverDokumenterRequest wsRequest = new OpprettJournalpostArkiverDokumenterRequest();
		wsRequest.setJournalpost(inngaaendeWsJournalpost);
		return wsRequest;
	}

}