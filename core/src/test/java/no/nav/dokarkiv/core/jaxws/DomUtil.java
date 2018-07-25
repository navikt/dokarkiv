package no.nav.dokarkiv.core.jaxws;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Utility for doing Dom stuff for testing
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public final class DomUtil {
	public static Element stringToElement(String xml) {
		DocumentBuilder documentBuilder;
		Document parse;
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			parse = documentBuilder.parse(new InputSource(new StringReader(xml)));
			return parse.getDocumentElement();
		} catch (SAXException | ParserConfigurationException | IOException e) {
			throw new IllegalStateException("Could not parse: " + xml, e);
		}
	}

	public static String elementToString(Element xmlElement) {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StringWriter stringWriter = new StringWriter();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(xmlElement), new StreamResult(stringWriter));
			return stringWriter.toString();
		} catch (TransformerException e) {
			throw new IllegalStateException("Could not parse element to String " + xmlElement, e);
		}
	}

	public static boolean evaluateXpathTrue(String xPathExpression, Object documentElement) throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		return (boolean) xPath.evaluate(xPathExpression, documentElement, XPathConstants.BOOLEAN);
	}
}
