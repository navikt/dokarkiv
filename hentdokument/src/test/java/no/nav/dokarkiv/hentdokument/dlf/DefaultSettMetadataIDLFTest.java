package no.nav.dokarkiv.hentdokument.dlf;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.hentdokument.dlf.to.SettMetadataForKopiering;
import no.nav.dokarkiv.hentdokument.dlf.to.SettMetadataForUthenting;
import no.nav.dokarkiv.hentdokument.dlf.to.SettMetadataIDLFRequest;
import no.nav.dokarkiv.hentdokument.dlf.to.SettMetadataIDLFResponse;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

/**
 * Unit tests for DefaultSettMetadataIDLF
 *
 * @author Per Kristian Foss, Visma Sirius
 */
public class DefaultSettMetadataIDLFTest {

	private static final String DEMO_DLF_FILENAME = "EESSI.dlf";
	private static final String CORRUPT_DLF_FILENAME = "Corrupt.dlf";
	private static byte[] dlfDokument;
	@Mock
	private SettMetadataIDlfXmlUpdater settMetadataIDlfXmlUpdaterMock;
	private SettMetadataForUthenting settMetadataForUthenting;
	private SettMetadataForKopiering settMetadataForKopiering;
	private SettMetadataIDLFRequest request;
	private DefaultSettMetadataIDLF settMetadataIDLF;
	private String filUuid_vedlegg = "96c7b818-e62a-4829-b67c-b7ceabbf7c07";
	private String filUuid = "96c7b818-e62a-4829-b67c-b7ceabbf7c06";

	private static byte[] loadFile(String filePath) throws IOException {
		File file = new ClassPathResource(filePath).getFile();
		return FileUtils.readFileToByteArray(file);
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		settMetadataIDLF = new DefaultSettMetadataIDLF();
		settMetadataIDLF.setSettMetadataIDlfXmlUpdater(settMetadataIDlfXmlUpdaterMock);
		settMetadataIDLF.setMetadataXmlEncoding("ISO-8859-1");

		settMetadataForUthenting = new SettMetadataForUthenting(1L, filUuid, 1L);
		settMetadataForKopiering = new SettMetadataForKopiering(2L, filUuid_vedlegg, loadFile("Demo.dlf"));
	}

	@Test
	public void shouldThrowExceptionWhenDlfDokumentIsNotSet() throws Exception {
		request = new SettMetadataIDLFRequest(settMetadataForUthenting, null);

		callServiceAndAssertErrorMessage("Missing parameter(s): dlfDokument ");
	}

	@Test
	public void shouldThrowExceptionWhenMandatoryParametersAreMissingFromLagringScenario() throws Exception {
		dlfDokument = loadFile(DEMO_DLF_FILENAME);
		settMetadataForUthenting = new SettMetadataForUthenting(null, "", null);
		request = new SettMetadataIDLFRequest(settMetadataForUthenting, dlfDokument);

		callServiceAndAssertErrorMessage("Missing parameter(s): journalpostId filUuid versjon");
	}

	@Test
	public void shouldThrowExceptionWhenMandatoryParametersAreMissingFromHentScenario() throws Exception {
		dlfDokument = loadFile(DEMO_DLF_FILENAME);
		settMetadataForKopiering = new SettMetadataForKopiering(null, "", null);
		request = new SettMetadataIDLFRequest(settMetadataForKopiering, dlfDokument);

		callServiceAndAssertErrorMessage("Missing parameter(s): journalpostIdVedlegg filUuidVedlegg dlfHoveddokument");
	}

	@Test
	public void shouldThrowExceptionWhenErrorOccursDuringUnzip() throws Exception {
		dlfDokument = loadFile(CORRUPT_DLF_FILENAME);
		request = new SettMetadataIDLFRequest(settMetadataForUthenting, dlfDokument);
		callServiceAndAssertErrorMessage("Error updating dlf");
	}

	@Test
	public void shouldCallXmlUpdaterWithMetadataXml() throws Exception {
		dlfDokument = loadFile(DEMO_DLF_FILENAME);
		SettMetadataIDLFRequest request = new SettMetadataIDLFRequest(settMetadataForUthenting, dlfDokument);
		String metadataXml = DlfTestUtils.getMetadataXml(dlfDokument);

		when(settMetadataIDlfXmlUpdaterMock.updateMetadataXmlForUthenting(isA(String.class),
				isA(SettMetadataForUthenting.class))).thenReturn("test");

		settMetadataIDLF.settMetadataIDLF(request);

		verify(settMetadataIDlfXmlUpdaterMock).updateMetadataXmlForUthenting(metadataXml, settMetadataForUthenting);
	}

	@Test
	public void shouldReturnDlfWithUpdatedXml() throws Exception {
		dlfDokument = loadFile(DEMO_DLF_FILENAME);
		SettMetadataIDLFRequest request = new SettMetadataIDLFRequest(settMetadataForKopiering, dlfDokument);

		String updatedXml = "<some><xml></xml></some>";
		when(settMetadataIDlfXmlUpdaterMock.updateMetadataXmlForKopiering(isA(String.class),
				isA(SettMetadataForKopiering.class), isA(String.class))).thenReturn(updatedXml);

		SettMetadataIDLFResponse response = settMetadataIDLF.settMetadataIDLF(request);

		assertThat(DlfTestUtils.getMetadataXml(response.getDlfDokument()), is(updatedXml));
	}

	private void callServiceAndAssertErrorMessage(String errorMessage) {
		try {
			settMetadataIDLF.settMetadataIDLF(request);
			fail("Should fail parameter validation");
		} catch (InvalidArgumentException e) {
			assertThat(e.getMessage(), containsString(errorMessage));
		}
	}

}
