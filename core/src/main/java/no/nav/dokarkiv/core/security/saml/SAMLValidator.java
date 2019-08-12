//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package no.nav.dokarkiv.core.security.saml;

import static no.nav.dokarkiv.core.security.saml.SamlUtils.buildSubject;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.jaxws.ThreadLocalSubjectHandler;
import no.nav.dokarkiv.core.security.AuthenticationResult;
import no.nav.modig.core.context.SubjectHandler;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.impl.AssertionUnmarshaller;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Slf4j
public class SAMLValidator {
	private Collection<X509Certificate> trustedCertificates;
	private SAMLSignatureProfileValidator profileValidator;
	private KeyStore keyStore;
	private int timeSkew;

	public SAMLValidator(String trustStore, String trustStorePassword) {
		this(trustStore, trustStorePassword, 0);
	}

	public SAMLValidator(String trustStore, String trustStorePassword, int timeSkewInMinutes) {
		this.timeSkew = timeSkewInMinutes;
		this.init();
		this.keyStore = new KeyStore(trustStore, trustStorePassword, (String) null);
		this.profileValidator = new SAMLSignatureProfileValidator();
	}

	private void init() {
		try {
			InitializationService.initialize();
		} catch (InitializationException var2) {
			throw new IllegalStateException("Feilet under initialisering av SAML", var2);
		}
	}

	private AuthenticationResult validateSignature(Assertion assertion) {
		BasicX509Credential credential;
		Signature signature;
		try {
			credential = this.getCredentialFromAssertion(assertion);
			signature = assertion.getSignature();
			X509Certificate certificateFromSignature = this.getCertificateFromSignature(signature);
			this.assertValidity(certificateFromSignature);
			if (!this.existsInTrustStore(certificateFromSignature) && !this.isSignedByTrustedCA(certificateFromSignature)) {
				return AuthenticationResult.invalid("Certificate not trusted, either it is missing from the truststore or not signed by a root CA in the truststore");
			}
		} catch (Exception var7) {
			return this.invalidResult(var7.getMessage(), var7);
		}

		try {
			this.getProfileValidator().validate(signature);
		} catch (SignatureException var6) {
			return this.invalidResult("Signature does not conform to spec", var6);
		}

		try {
			SignatureValidator.validate(signature, credential);
		} catch (SignatureException var5) {
			return this.invalidResult("Signature not valid", var5);
		}

		return AuthenticationResult.success(this.getUsernameFromNameID(assertion), this.getAttribute(assertion, "consumerId"));
	}

	private boolean isSignedByTrustedCA(X509Certificate certificateInAssertion) throws SAMLException {
		Optional<X509Certificate> rootCACertificate = this.findSigningRootCACertificate(certificateInAssertion);
		if (rootCACertificate.isPresent()) {
			this.assertValidity((X509Certificate) rootCACertificate.get());
			return true;
		} else {
			return false;
		}
	}

	private Optional<X509Certificate> findSigningRootCACertificate(X509Certificate certificateInAssertion) {
		Iterator var2 = this.getTrustedCertificates().iterator();

		while (true) {
			X509Certificate certificate;
			boolean isCARoot;
			do {
				if (!var2.hasNext()) {
					return Optional.empty();
				}

				certificate = (X509Certificate) var2.next();
				isCARoot = certificate.getBasicConstraints() != -1;
			} while (!isCARoot);

			String certCN = certificate.getSubjectDN().getName();

			try {
				certificateInAssertion.verify(certificate.getPublicKey());
				log.debug("Certificate from SAML assertion is signed by root CA certificate with CN {}", certCN);
				return Optional.of(certificate);
			} catch (Exception var7) {
				log.trace("Certificate({}) was not used to sign Certificate from SAML assertion", certCN);
			}
		}
	}

	public AuthenticationResult validateSAMLAssertion(Element assertionElement) {
		try {
			Assertion assertion = (Assertion) (new AssertionUnmarshaller()).unmarshall(assertionElement);
			AuthenticationResult authenticationResult = this.notExpired(assertion) ? this.validateSignature(assertion) : AuthenticationResult.invalid(String.format("Assertion in token has expired. NotBefore: %s NotAfter: %s Current: %s", assertion.getConditions()
					.getNotBefore(), assertion.getConditions().getNotOnOrAfter(), LocalDateTime.now()));
			if (authenticationResult.isValid()) {
				((ThreadLocalSubjectHandler) SubjectHandler.getSubjectHandler()).setSubject(buildSubject(assertionElement));
			}
			return authenticationResult;
		} catch (UnmarshallingException var4) {
			return this.invalidResult("Failed while unmarshalling assertion", var4);
		}
	}

	private AuthenticationResult invalidResult(String msg, Exception e) {
		log.error(msg, e);
		return AuthenticationResult.invalid(msg);
	}

	private boolean notExpired(Assertion assertion) {
		DateTime notBefore = assertion.getConditions().getNotBefore().minusMinutes(this.timeSkew);
		DateTime notOnOrAfter = assertion.getConditions().getNotOnOrAfter().plusMinutes(this.timeSkew);
		return notBefore.isBeforeNow() && notOnOrAfter.isAfterNow();
	}

	private void assertValidity(X509Certificate certificate) throws SAMLException {
		try {
			certificate.checkValidity();
		} catch (CertificateNotYetValidException | CertificateExpiredException var3) {
			throw new SAMLException("Certificate failed validity check", var3);
		}
	}

	private boolean existsInTrustStore(X509Certificate x509Certificate) throws SAMLException {
		boolean isTrusted = false;

		try {
			String certificateAlias = this.getSakKeyStore().getKeyStore().getCertificateAlias(x509Certificate);
			if (!StringUtils.isEmpty(certificateAlias)) {
				isTrusted = true;
			}

			return isTrusted;
		} catch (KeyStoreException var4) {
			throw new SAMLException("Failed while getting SAML assertion certificate from keystore", var4);
		}
	}

	private Collection<X509Certificate> getTrustedCertificates() {
		if (this.trustedCertificates == null) {
			this.trustedCertificates = this.getSakKeyStore().getCertificates();
		}

		return this.trustedCertificates;
	}

	private BasicX509Credential getCredentialFromAssertion(Assertion assertion) throws Exception {
		X509Certificate x509Certificate = this.getCertificateFromSignature(assertion.getSignature());
		return new BasicX509Credential(x509Certificate);
	}

	private X509Certificate getCertificateFromSignature(Signature signature) throws Exception {
		org.opensaml.xmlsec.signature.X509Certificate certificate = (org.opensaml.xmlsec.signature.X509Certificate) ((X509Data) signature.getKeyInfo().getX509Datas().get(0)).getX509Certificates().get(0);
		if (certificate == null) {
			throw new SAMLException("Unable to retrieve certificate from signature");
		} else {
			byte[] bytes = Base64.decodeBase64(certificate.getValue());
			return this.createCertificate(new ByteArrayInputStream(bytes));
		}
	}

	private X509Certificate createCertificate(InputStream stream) throws Exception {
		try {
			BufferedInputStream bis = new BufferedInputStream(stream);

			X509Certificate x509Certificate;
			try {
				CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
				x509Certificate = (X509Certificate) certificateFactory.generateCertificate(bis);
			} catch (Throwable var7) {
				try {
					bis.close();
				} catch (Throwable var6) {
					var7.addSuppressed(var6);
				}

				throw var7;
			}

			bis.close();
			return x509Certificate;
		} catch (CertificateException | IOException var8) {
			throw new SAMLException("Could not create certificate from input stream", var8);
		}
	}

	private String getAttribute(Assertion assertion, String attributeName) {
		List<Attribute> attributes = assertion.getAttributeStatements().get(0).getAttributes();
		Attribute attribute = attributes.stream().filter((attr) -> attributeName.equals(attr.getName())).findFirst()
				.orElseThrow(() -> new RuntimeException("Missing attribute in SAML assertion: " + attributeName));
		XMLObject xmlObject = attribute.getAttributeValues().get(0);
		String textContent;
		if (xmlObject instanceof XSString) {
			textContent = ((XSString) xmlObject).getValue();
		} else {
			if (!(xmlObject instanceof XSAnyImpl)) {
				throw new RuntimeException(String.format("Could not retrieve attribute %s from saml assertion", attributeName));
			}

			textContent = ((XSAnyImpl) xmlObject).getTextContent();
		}

		return textContent;
	}

	private String getUsernameFromNameID(Assertion assertion) {
		return assertion.getSubject().getNameID().getValue();
	}

	private Element createXMLElementFromToken(String tokenBase64) throws ParserConfigurationException, SAXException, IOException {
		byte[] bytes = Base64.decodeBase64(tokenBase64.getBytes("UTF-8"));
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
		Document parse = db.parse(new ByteArrayInputStream(bytes));
		return parse.getDocumentElement();
	}

	private SAMLSignatureProfileValidator getProfileValidator() {
		return this.profileValidator;
	}

	private KeyStore getSakKeyStore() {
		return this.keyStore;
	}

	public AuthenticationResult validate(String tokenBase64) {
		Element assertionElement;
		try {
			assertionElement = this.createXMLElementFromToken(tokenBase64);
		} catch (Exception var4) {
			return this.invalidResult("Failed while creating XML element from token", var4);
		}

		return this.validateSAMLAssertion(assertionElement);
	}

}
