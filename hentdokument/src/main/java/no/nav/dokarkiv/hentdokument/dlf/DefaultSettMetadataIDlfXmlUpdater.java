package no.nav.dokarkiv.hentdokument.dlf;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.MetadataXmlUpdateFailedException;
import no.nav.dokarkiv.hentdokument.dlf.to.SettMetadataForKopiering;
import no.nav.dokarkiv.hentdokument.dlf.to.SettMetadataForUthenting;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of SettMetadataIDlfXmlUpdater.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@Component
public class DefaultSettMetadataIDlfXmlUpdater implements SettMetadataIDlfXmlUpdater {

	private static final String JOURNALPOST_ID_TAG = "journalpostID";
	private static final String FIL_UUID_TAG = "filUUID";
	private static final String VERSJON_TAG = "versjon";
	private static final String JOURNALPOST_ID_VEDLEGG_TAG = "journalpostID_vedlegg";
	private static final String FIL_UUID_VEDLEGG_TAG = "filUUID_vedlegg";
	private static final String ESB_ENDPOINT_URL_JOARK_TAG = "ESBendpointURL";
	private static final String ESB_USER_ID_TAG = "ESBuserId";
	private static final String ESB_***passord=gammelt_passord***";
	private static final String ESB_ENDPOINT_URL_HP_LIVE_TAG = "ArkivDokumentURL";
	private static final String VEDLEGG_URL_TAG = "ArkivPdfVedleggURL";

	private VedleggUrlRetriever vedleggUrlRetriever;
	private String metadataXmlEncoding;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String updateMetadataXmlForUthenting(String metadataXml, SettMetadataForUthenting metadataForUthenting) {
		Document metadata = getMetadataDocument(metadataXml);
		updateMetadataWithUthentingValues(metadata, metadataForUthenting);
		return transformDocumentToXml(metadata);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String updateMetadataXmlForKopiering(String metadataXml, SettMetadataForKopiering metadataForKopiering,
												String hoveddokumentMetadataXml) {
		Document metadata = getMetadataDocument(metadataXml);
		updateMetadataWithKopieringValues(metadata, metadataForKopiering, hoveddokumentMetadataXml);
		return transformDocumentToXml(metadata);
	}

	private Document getMetadataDocument(String metadataXml) {
		Document metadata = null;
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			metadata = documentBuilder.parse(new InputSource(new StringReader(metadataXml)));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new MetadataXmlUpdateFailedException(e);
		}
		return metadata;
	}

	private void updateMetadataWithUthentingValues(Document metadata, SettMetadataForUthenting metadataForUthenting) {
		validateElements(metadata, Arrays.asList(JOURNALPOST_ID_TAG, FIL_UUID_TAG, VERSJON_TAG));
		updateElementWithTextContent(metadata, JOURNALPOST_ID_TAG, metadataForUthenting.getJournalpostId().toString());
		updateElementWithTextContent(metadata, FIL_UUID_TAG, metadataForUthenting.getFilUuid());
		updateElementWithTextContent(metadata, VERSJON_TAG, metadataForUthenting.getVersjon().toString());

		updateMetadataWithVedleggUrl(metadata);
	}

	private void updateMetadataWithVedleggUrl(Document metadata) {
		if (hasTextContent(metadata, JOURNALPOST_ID_VEDLEGG_TAG)) {
			validateElements(metadata, Arrays.asList(VEDLEGG_URL_TAG));
			String vedleggUrl = vedleggUrlRetriever.retrieveVedleggUrl(
					getTextContentFromElement(metadata, JOURNALPOST_ID_VEDLEGG_TAG),
					getTextContentFromElement(metadata, FIL_UUID_VEDLEGG_TAG));
			updateElementWithTextContent(metadata, VEDLEGG_URL_TAG, vedleggUrl);
		}
	}

	private void updateMetadataWithKopieringValues(Document metadata, SettMetadataForKopiering metadataForKopiering,
												   String hoveddokumentMetadataXml) {
		validateElements(metadata, Arrays.asList(JOURNALPOST_ID_VEDLEGG_TAG, FIL_UUID_VEDLEGG_TAG, ESB_ENDPOINT_URL_JOARK_TAG,
				ESB_USER_ID_TAG, ESB_PASSWORD_TAG, ESB_ENDPOINT_URL_HP_LIVE_TAG));

		EnvironmentConfig environmentConfig = extractEnvironmentConfigFrom(hoveddokumentMetadataXml);
		updateElementWithTextContent(metadata, ESB_ENDPOINT_URL_JOARK_TAG, environmentConfig.getEsbEndpointUrlJoark());
		updateElementWithTextContent(metadata, ESB_USER_ID_TAG, environmentConfig.getEsbUserId());
		updateElementWithTextContent(metadata, ESB_PASSWORD_TAG, environmentConfig.getEsbPassword());
		updateElementWithTextContent(metadata, ESB_ENDPOINT_URL_HP_LIVE_TAG, environmentConfig.getEsbEndpointUrlHpLive());

		updateElementWithTextContent(metadata, JOURNALPOST_ID_VEDLEGG_TAG, metadataForKopiering.getJournalpostIdVedlegg()
				.toString());
		updateElementWithTextContent(metadata, FIL_UUID_VEDLEGG_TAG, metadataForKopiering.getFilUuidVedlegg());
	}

	private void validateElements(Document document, List<String> elementsToValidate) {
		for (String tagName : elementsToValidate) {
			if (getElementCount(document, tagName) != 1) {
				throw new InvalidArgumentException("Xml does not contain element <" + tagName
						+ ">, or it occurs multiple times");
			}
		}
	}

	private int getElementCount(Document document, String tagName) {
		return document.getElementsByTagName(tagName).getLength();
	}

	private void updateElementWithTextContent(Document metadata, String tagName, String textContent) {
		metadata.getElementsByTagName(tagName).item(0).setTextContent(textContent);
	}

	private boolean hasTextContent(Document document, String tagName) {
		return isNotBlank(getTextContentFromElement(document, tagName));
	}

	private String transformDocumentToXml(Document metadata) throws TransformerFactoryConfigurationError {
		Writer writer = new StringWriter();
		try {
			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

			final Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, metadataXmlEncoding);

			transformer.transform(new DOMSource(metadata), new StreamResult(writer));
		} catch (TransformerException e) {
			throw new MetadataXmlUpdateFailedException(e);
		}
		return writer.toString();
	}

	private EnvironmentConfig extractEnvironmentConfigFrom(String hoveddokumentMetadataXml) {
		Document document = getMetadataDocument(hoveddokumentMetadataXml);
		validateElements(document,
				Arrays.asList(ESB_ENDPOINT_URL_JOARK_TAG, ESB_USER_ID_TAG, ESB_PASSWORD_TAG, ESB_ENDPOINT_URL_HP_LIVE_TAG));

		EnvironmentConfig environmentConfig = new EnvironmentConfig();
		environmentConfig.setEsbEndpointUrlJoark(getTextContentFromElement(document, ESB_ENDPOINT_URL_JOARK_TAG));
		environmentConfig.setEsbUserId(getTextContentFromElement(document, ESB_USER_ID_TAG));
		environmentConfig.setEsbPassword(getTextContentFromElement(document, ESB_PASSWORD_TAG));
		environmentConfig.setEsbEndpointUrlHpLive(getTextContentFromElement(document, ESB_ENDPOINT_URL_HP_LIVE_TAG));
		return environmentConfig;
	}

	private String getTextContentFromElement(Document document, String tagName) {
		return document.getElementsByTagName(tagName).item(0).getTextContent();
	}

	/**
	 * Setter for the vedleggUrlRetriever property.
	 *
	 * @param vedleggUrlRetriever the vedleggUrlRetriever to set
	 */
	public void setVedleggUrlRetriever(VedleggUrlRetriever vedleggUrlRetriever) {
		this.vedleggUrlRetriever = vedleggUrlRetriever;
	}

	/**
	 * Setter for the metadataXmlEncoding property.
	 *
	 * @param metadataXmlEncoding the metadataXmlEncoding to set
	 */
	public void setMetadataXmlEncoding(String metadataXmlEncoding) {
		this.metadataXmlEncoding = metadataXmlEncoding;
	}

	/**
	 * Holds the environment config that is copied from hoveddokument to vedlegg.
	 */
	private static class EnvironmentConfig {
		private String esbEndpointUrlJoark;
		private String esbUserId;
		private String esbPassword;
		private String esbEndpointUrlHpLive;

		public String getEsbEndpointUrlJoark() {
			return esbEndpointUrlJoark;
		}

		public void setEsbEndpointUrlJoark(String esbEndpointUrlJoark) {
			this.esbEndpointUrlJoark = esbEndpointUrlJoark;
		}

		public String getEsbUserId() {
			return esbUserId;
		}

		public void setEsbUserId(String esbUserId) {
			this.esbUserId = esbUserId;
		}

		public String getEsbPassword() {
			return esbPassword;
		}

		public void setEsbPassword(String esbPassword) {
			this.esb***passord=gammelt_passord***;
		}

		public String getEsbEndpointUrlHpLive() {
			return esbEndpointUrlHpLive;
		}

		public void setEsbEndpointUrlHpLive(String esbEndpointUrlHpLive) {
			this.esbEndpointUrlHpLive = esbEndpointUrlHpLive;
		}
	}

}
