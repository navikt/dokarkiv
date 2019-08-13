package no.nav.dokarkiv.sak.util;

import no.nav.dokarkiv.core.security.saml.KeyStore;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.saml.common.SAMLObjectContentReference;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml.saml2.core.impl.AssertionMarshaller;
import org.opensaml.saml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.saml.saml2.core.impl.ConditionsBuilder;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml.saml2.core.impl.SubjectBuilder;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.impl.KeyInfoBuilder;
import org.opensaml.xmlsec.signature.impl.SignatureBuilder;
import org.opensaml.xmlsec.signature.impl.X509CertificateBuilder;
import org.opensaml.xmlsec.signature.impl.X509DataBuilder;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.w3c.dom.Element;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

public class SAMLSupport {

    private final KeyStore keyStore;

    public SAMLSupport(String trustStore, String trustStorePassword, String privateKeyPassword) {
        keyStore = new KeyStore(trustStore, trustStorePassword, privateKeyPassword);

        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            throw new IllegalStateException("Failed to initialize SAML test support class", e);
        }
    }

    public String createNewToken() {
        return createNewToken(DateTime.now(), DateTime.now().plusDays(1));
    }

    public String createNewToken(DateTime notBefore, DateTime notOnOrAfter) {
        ConditionsBuilder cb = new ConditionsBuilder();
        Conditions conditions = cb.buildObject();
        conditions.setNotBefore(notBefore);
        conditions.setNotOnOrAfter(notOnOrAfter);

        NameIDBuilder nb = new NameIDBuilder();
        NameID nameId = nb.buildObject();
        nameId.setValue("testbruker");
        SubjectBuilder sb = new SubjectBuilder();
        Subject sub = sb.buildObject();
        sub.setNameID(nameId);

        IssuerBuilder ib = new IssuerBuilder();
        Issuer issuer = ib.buildObject();
        issuer.setValue("NAV_SIKKERHET_TEST");

        Attribute attributeAuthLevel = createAttribute("authenticationLevel", "4");
        Attribute attributeIdentType = createAttribute("identType", "InternBruker");
        Attribute attributeConsumerId = createAttribute("consumerId", "sikkerhetTest");

        AttributeStatementBuilder asb = new AttributeStatementBuilder();
        AttributeStatement attributeStatement = asb.buildObject();
        attributeStatement.getAttributes().add(attributeAuthLevel);
        attributeStatement.getAttributes().add(attributeIdentType);
        attributeStatement.getAttributes().add(attributeConsumerId);

        AssertionBuilder ab = new AssertionBuilder();
        Assertion assertion = ab.buildObject();
        assertion.setIssueInstant(DateTime.now());
        assertion.setID("123");
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setSubject(sub);
        assertion.getAttributeStatements().add(attributeStatement);
        assertion.setIssuer(issuer);
        assertion.setConditions(conditions);

        Element element = signAssertion(assertion);
        return Base64.encodeBase64String(getSamlAssertionAsByteArray(element));
    }

    private KeyStore getKeyStore() {
        return keyStore;
    }

    private Element signAssertion(Assertion assertion) {
        java.security.KeyStore.PrivateKeyEntry privateKeyEntry = getKeyStore().getSSLCertificate();
        X509Certificate cert = (X509Certificate) privateKeyEntry.getCertificate();
        BasicX509Credential x509Credential = new BasicX509Credential(cert);

        PrivateKey privateKey = privateKeyEntry.getPrivateKey();
        x509Credential.setPrivateKey(privateKey);

        SignatureBuilder signatureBuilder = new SignatureBuilder();

        Signature signature = signatureBuilder.buildObject();
        signature.setSigningCredential(x509Credential);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

        KeyInfoBuilder keyInfoBuilder = new KeyInfoBuilder();
        KeyInfo keyInfo = keyInfoBuilder.buildObject();

        X509DataBuilder x509DataBuilder = new X509DataBuilder();

        X509CertificateBuilder x509CertificateBuilder = new X509CertificateBuilder();
        org.opensaml.xmlsec.signature.X509Certificate certificate = x509CertificateBuilder.buildObject();
        try {
            certificate.setValue(Base64.encodeBase64String(x509Credential.getEntityCertificate().getEncoded()));
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }

        X509Data data = x509DataBuilder.buildObject();
        data.getX509Certificates().add(certificate);

        keyInfo.getX509Datas().add(data);
        signature.setKeyInfo(keyInfo);

        assertion.setSignature(signature);

        ((SAMLObjectContentReference) signature.getContentReferences().get(0))
            .setDigestAlgorithm(SignatureConstants.ALGO_ID_DIGEST_SHA256);
        Element element;
        try {
            element = new AssertionMarshaller().marshall(assertion);
        } catch (MarshallingException e) {
            throw new RuntimeException("Failed to marshall assertion", e);
        }

        try {
            Signer.signObject(signature);
        } catch (SignatureException e) {
            throw new RuntimeException("Failed to sign assertion", e);
        }
        return element;
    }

    private Attribute createAttribute(String name, String value) {
        AttributeBuilder attrb = new AttributeBuilder();
        Attribute attribute = attrb.buildObject();
        attribute.setName(name);
        attribute.setNameFormat(Attribute.URI_REFERENCE);
        XSStringBuilder stringBuilder = new XSStringBuilder();
        XSString attributeValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        attributeValue.setValue(value);
        attribute.getAttributeValues().add(attributeValue);
        return attribute;
    }

    private byte[] getSamlAssertionAsByteArray(Element samlAssertionElement) {
        StringWriter writer = new StringWriter();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(samlAssertionElement), new StreamResult(writer));
        } catch (TransformerException e) {
            throw new RuntimeException("Failed while parsing SAML assertion element", e);
        }
        return writer.toString().getBytes(Charset.forName("UTF-8"));
    }
}

