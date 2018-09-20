package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterDataUtil.DOKUMENT_INNHOLD_BASE64;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterDataUtil.FILREFERANSE_S3;
import static no.nav.dokarkiv.core.storage.DokprodMellomlagerS3Storage.DOKPRODMELLOMLAGER_DIRECTORY_NAME;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.sporing.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.storage.Storage;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumenterRequest;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

/**
 * Test class for
 * DefaultOpprettJournalpostArkiverDokumentRequestMapper.
 *
 * @author Stig Strøm
 */
public class OpprettJournalpostArkiverDokumenterRequestMapperTest {

	private KildeNavnPopulator kildeNavnPopulatorMock = mock(KildeNavnPopulator.class);
	private Storage storageMock = mock(Storage.class);

	private OpprettJournalpostArkiverDokumenterRequestMapper requestMapper = new OpprettJournalpostArkiverDokumenterRequestMapper(kildeNavnPopulatorMock, storageMock);

	@Before
	public void setUp() throws Exception {
		DateProvider.configure(true, "2014-08-27T12:00:00");
		createRequest();
		RequestContextSetter.setRequestContextForUnitTest();
	}

	@Test
	public void shouldMapOpprettOgFerdigstillRequestToTransferObject() throws Exception {
		when(storageMock.get(eq(DOKPRODMELLOMLAGER_DIRECTORY_NAME), eq(FILREFERANSE_S3))).thenReturn(Optional.of("{\n" +
				"  \"axml\" : \"" + DOKUMENT_INNHOLD_BASE64 + "\",\n" +
				"  \"pdf\": \"" + DOKUMENT_INNHOLD_BASE64 + "\"\n" +
				"}"));

		OpprettJournalpostArkiverDokumenterRequestTo domainRequest = requestMapper.map(createRequest());
		no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost = domainRequest.getJournalpost();
		OpprettJournalpostArkiverDokumenterAssertUtil.assertEqualJournalposts(domainJournalpost);
	}

	private OpprettJournalpostArkiverDokumenterRequest createRequest() throws Exception {
		Journalpost inngaaendeWsJournalpost = OpprettJournalpostArkiverDokumenterDataUtil.createJournalpost();
		OpprettJournalpostArkiverDokumenterRequest wsRequest = new OpprettJournalpostArkiverDokumenterRequest();
		wsRequest.setJournalpost(inngaaendeWsJournalpost);
		return wsRequest;
	}


}