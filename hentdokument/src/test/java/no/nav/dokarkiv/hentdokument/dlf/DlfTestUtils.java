package no.nav.dokarkiv.hentdokument.dlf;

import com.google.common.io.CharStreams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Contains utils useful for testing dlf manipulation.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public final class DlfTestUtils {

	private DlfTestUtils() {
	}

	public static String getMetadataXml(byte[] dlf) throws IOException {
		ZipInputStream inputDlfStream = new ZipInputStream(new ByteArrayInputStream(dlf));
		ZipEntry entry;
		String metadataXml = null;
		while ((entry = inputDlfStream.getNextEntry()) != null) {
			if (entry.getName().equals(DefaultSettMetadataIDLF.DLF_METADATA_XML_FILE)) {
				metadataXml = CharStreams.toString(new InputStreamReader(inputDlfStream, "ISO-8859-1"));
				break;
			}
			inputDlfStream.closeEntry();
		}
		inputDlfStream.close();
		return metadataXml;
	}
}
