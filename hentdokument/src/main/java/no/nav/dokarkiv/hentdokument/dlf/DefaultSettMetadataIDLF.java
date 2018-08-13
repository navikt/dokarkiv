package no.nav.dokarkiv.hentdokument.dlf;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import no.nav.dokarkiv.core.dokumenturl.AbstractDocumentOperation;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.hentdokument.dlf.to.SettMetadataIDLFRequest;
import no.nav.dokarkiv.hentdokument.dlf.to.SettMetadataIDLFResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Implementation of SettMetadataIDLF.
 *
 * @author Per Kristian Foss, Visma Sirius
 */
@Component
public class DefaultSettMetadataIDLF extends AbstractDocumentOperation implements SettMetadataIDLF {

	/**
	 * Name of the xml file containing metadata inside the dlf file
	 */
	public static final String DLF_METADATA_XML_FILE = "DLF/D/Customer 1.xml";

	@Inject
	private SettMetadataIDlfXmlUpdater settMetadataIDlfXmlUpdater;
	@Value("${hentdokument.dlf.metadataXmlEncoding}")
	private String metadataXmlEncoding;

	@Override
	public SettMetadataIDLFResponse settMetadataIDLF(SettMetadataIDLFRequest settMetadataIDLFRequest) {
		validateRequest(settMetadataIDLFRequest);

		byte[] updatedDLFDocument = parseAndUpdateDlf(settMetadataIDLFRequest);

		return new SettMetadataIDLFResponse(updatedDLFDocument);
	}

	private void validateRequest(SettMetadataIDLFRequest settMetadataIDLFRequest) {
		if (settMetadataIDLFRequest == null) {
			throw new InvalidArgumentException("Missing parameter", "SettMetadataIDLFRequest", null);
		}
		settMetadataIDLFRequest.validate();
	}

	private byte[] parseAndUpdateDlf(SettMetadataIDLFRequest request) {
		ZipInputStream inputDlfStream = new ZipInputStream(new ByteArrayInputStream(request.getDlfDokument()));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ZipOutputStream outputDlfStream = new ZipOutputStream(bos);
		try {
			boolean metadataEntryExists = parseEntries(inputDlfStream, outputDlfStream, request);
			if (!metadataEntryExists) {
				throw new InvalidArgumentException("Could not find Customer 1.xml in dlfDokument");
			}
		} catch (IOException e) {
			throw new InvalidArgumentException("Error updating dlf: ", e);
		} finally {
			try {
				inputDlfStream.close();
				outputDlfStream.close();
			} catch (IOException e) { //nosonar
				// Nothing more to do
			}
		}
		return bos.toByteArray();
	}

	private boolean parseEntries(ZipInputStream inputDlfStream, ZipOutputStream outputDlfStream,
								 SettMetadataIDLFRequest request) throws IOException {
		ZipEntry entry;
		boolean foundmetadataEntry = false;
		while ((entry = inputDlfStream.getNextEntry()) != null) {
			if (entry.getName().equals(DLF_METADATA_XML_FILE)) {
				foundmetadataEntry = true;
				outputDlfStream.putNextEntry(new ZipEntry(DLF_METADATA_XML_FILE));
				outputDlfStream.write(updateMetadataXml(convertToXml(inputDlfStream), request));
			} else {
				outputDlfStream.putNextEntry(new ZipEntry(entry.getName()));
				ByteStreams.copy(inputDlfStream, outputDlfStream);
			}
			inputDlfStream.closeEntry();
		}
		return foundmetadataEntry;
	}

	private String convertToXml(ZipInputStream inputDlfStream) throws IOException {
		return CharStreams.toString(new InputStreamReader(inputDlfStream, metadataXmlEncoding));
	}

	private byte[] updateMetadataXml(String metadataXml, SettMetadataIDLFRequest request) throws IOException {
		String updatedMetadataXml = null;
		if (request.isForUthenting()) {
			updatedMetadataXml = settMetadataIDlfXmlUpdater.updateMetadataXmlForUthenting(metadataXml,
					request.getSettMetadataForUthenting());
		} else {
			String hoveddokumentMetadataXml = getMetadataXml(request.getSettMetadataForKopiering().getDlfHoveddokument());
			updatedMetadataXml = settMetadataIDlfXmlUpdater.updateMetadataXmlForKopiering(metadataXml,
					request.getSettMetadataForKopiering(), hoveddokumentMetadataXml);
		}
		return updatedMetadataXml.getBytes(metadataXmlEncoding);
	}

	private String getMetadataXml(byte[] dlf) throws IOException {
		ZipInputStream inputDlfStream = new ZipInputStream(new ByteArrayInputStream(dlf));
		ZipEntry entry;
		String metadataXml = null;
		try {
			while ((entry = inputDlfStream.getNextEntry()) != null) {
				if (entry.getName().equals(DLF_METADATA_XML_FILE)) {
					metadataXml = convertToXml(inputDlfStream);
					break;
				}
				inputDlfStream.closeEntry();
			}
		} finally {
			inputDlfStream.close();
		}
		return metadataXml;
	}

	public void setSettMetadataIDlfXmlUpdater(SettMetadataIDlfXmlUpdater settMetadataIDlfXmlUpdater) {
		this.settMetadataIDlfXmlUpdater = settMetadataIDlfXmlUpdater;
	}

	public void setMetadataXmlEncoding(String metadataXmlEncoding) {
		this.metadataXmlEncoding = metadataXmlEncoding;
	}
}
