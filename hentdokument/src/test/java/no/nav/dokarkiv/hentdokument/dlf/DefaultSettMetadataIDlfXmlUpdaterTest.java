package no.nav.dokarkiv.hentdokument.dlf;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import com.google.common.io.Files;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.hentdokument.dlf.to.SettMetadataForKopiering;
import no.nav.dokarkiv.hentdokument.dlf.to.SettMetadataForUthenting;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.nio.charset.Charset;

/**
 * Unit tests for DefaultSettMetadataIDlfXmlUpdater.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class DefaultSettMetadataIDlfXmlUpdaterTest {

	private static String metadataXml;
	private static String vedleggMalMetadataXml;
	private static String hoveddokumentXml;

	private static String encoding = "ISO-8859-1";

	@Mock
	private DefaultVedleggUrlRetriever vedleggUrlRetrieverMock;

	private DefaultSettMetadataIDlfXmlUpdater xmlUpdater;

	@BeforeClass
	public static void init() throws Exception {
		metadataXml = Files.toString(new File("src/test/resources/Customer1.xml"), Charset.forName(encoding));
		vedleggMalMetadataXml = Files.toString(new File("src/test/resources/vedleggmal.xml"), Charset.forName(encoding));
		hoveddokumentXml = Files.toString(new File("src/test/resources/hoveddokument.xml"), Charset.forName(encoding));
	}

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		xmlUpdater = new DefaultSettMetadataIDlfXmlUpdater(vedleggUrlRetrieverMock, encoding);
	}

	@Test
	public void shouldThrowExceptionWhenMetadataTagsDoesNotExist() throws Exception {
		try {
			xmlUpdater.updateMetadataXmlForUthenting("<test/>", new SettMetadataForUthenting(null, null, null));
			fail("Expected exception");
		} catch (InvalidArgumentException e) {
			assertThat(e.getMessage(), containsString("Xml does not contain element"));
		}
	}

	@Test
	public void shouldUpdateMetadataForUthenting() throws Exception {
		Long journalpostId = 100L;
		Long versjon = 1L;
		String filUuid = "123-123-123-123";
		SettMetadataForUthenting metadataForUthenting = new SettMetadataForUthenting(journalpostId, filUuid, versjon);

		String updatedMetadataXml = xmlUpdater.updateMetadataXmlForUthenting(metadataXml, metadataForUthenting);

		assertThat(updatedMetadataXml, containsString("<journalpostID>" + journalpostId + "</journalpostID>"));
		assertThat(updatedMetadataXml, containsString("<filUUID>" + filUuid + "</filUUID>"));
		assertThat(updatedMetadataXml, containsString("<versjon>" + versjon + "</versjon>"));
	}

	@Test
	public void shouldUpdateMetadataForUthentingWithVedleggUrl() throws Exception {
		String vedleggUrl = "http://wasapp.adeo.no/joarkweb/HentDokument?docToken=123123123";

		// Values from vedleggmal.xml
		String journalpostIdVedlegg = "300123123";
		String filUuidVedlegg = "12312312-123123-123123-123";
		when(vedleggUrlRetrieverMock.retrieveVedleggUrl(journalpostIdVedlegg, filUuidVedlegg)).thenReturn(vedleggUrl);

		String updatedMetadataXml = xmlUpdater.updateMetadataXmlForUthenting(vedleggMalMetadataXml,
				new SettMetadataForUthenting(1L, "1", 1L));

		assertThat(updatedMetadataXml, containsString("<ArkivPdfVedleggURL>" + vedleggUrl + "</ArkivPdfVedleggURL>"));
	}

	@Test
	public void shouldUpdateMetadataForKopiering() throws Exception {
		Long journalpostIdVedlegg = 200L;
		String filUuidVedlegg = "345-345-345-345";
		SettMetadataForKopiering metadataForKopiering = new SettMetadataForKopiering(journalpostIdVedlegg, filUuidVedlegg,
				null);

		String updatedMetadataXml = xmlUpdater.updateMetadataXmlForKopiering(metadataXml, metadataForKopiering,
				hoveddokumentXml);

		assertThat(updatedMetadataXml, containsString("<journalpostID_vedlegg>" + journalpostIdVedlegg
				+ "</journalpostID_vedlegg>"));
		assertThat(updatedMetadataXml, containsString("<filUUID_vedlegg>" + filUuidVedlegg + "</filUUID_vedlegg>"));
	}

	@Test
	public void shouldUpdateMetadataForKopieringWithMetadataFromHoveddokument() throws Exception {
		// Values in hoveddokument.xml
		String endpointJoark = "https://tjenestebuss.adeo.no/nav-tjeneste-journalbehandling_v1Web/";
		String user = "srvLiveEditor";
		String ***passord=gammelt_passord***";
		String endpointHpLive = "https://tjenestebuss.adeo.no/nav-tjeneste-hplivejournalbehandling_v1Web/";

		String updatedMetadataXml = xmlUpdater.updateMetadataXmlForKopiering(metadataXml, new SettMetadataForKopiering(1L, "1",
				null), hoveddokumentXml);

		assertThat(updatedMetadataXml, containsString("<ESBendpointURL>" + endpointJoark + "</ESBendpointURL>"));
		assertThat(updatedMetadataXml, containsString("<ESBuserId>" + user + "</ESBuserId>"));
		assertThat(updatedMetadataXml, containsString("<ESBpasswordText>" + password + "</ESBpasswordText>"));
		assertThat(updatedMetadataXml, containsString("<ArkivDokumentURL>" + endpointHpLive + "</ArkivDokumentURL>"));
	}

}
