package no.nav.dokarkiv.core.security;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.jaxws.ThreadLocalSubjectHandler;
import no.nav.modig.core.context.AuthenticationLevelCredential;
import no.nav.modig.core.context.SAMLAssertionCredential;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.core.domain.ConsumerId;
import no.nav.modig.core.domain.IdentType;
import no.nav.modig.core.domain.SluttBruker;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.rt.security.saml.claims.SAMLSecurityContext;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.principal.SAMLTokenPrincipal;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.saml.saml2.core.Attribute;

import javax.security.auth.Subject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * CXF Soap interceptor som kun validerer SAML-token (logger ikke caller inn i containeren).
 */
@Slf4j
public class ValidateSamlInInterceptor extends WSS4JInInterceptor {

	private static final List<String> PING_ACTIONS = Arrays.asList(
			"http://nav.no/tjeneste/virksomhet/behandleJournal/v2/behandleJournal_v2/pingRequest",
			"http://nav.no/tjeneste/virksomhet/innsynJournal/v2/InnsynJournal_v2/pingRequest",
			"http://nav.no/tjeneste/virksomhet/inngaaendeJournal/v1/InngaaendeJournal_v1/pingRequest",
			"http://nav.no/tjeneste/virksomhet/behandleInngaaendeJournal/v1/BehandleInngaaendeJournal_v1/pingRequest");

	public ValidateSamlInInterceptor() {
		super();
		setProperty(WSHandlerConstants.ACTION, WSHandlerConstants.SAML_TOKEN_SIGNED);
	}

	public ValidateSamlInInterceptor(boolean ignore) {
		super(ignore);
		setProperty(WSHandlerConstants.ACTION, WSHandlerConstants.SAML_TOKEN_SIGNED);
	}

	public ValidateSamlInInterceptor(Map<String, Object> properties) {
		super(properties);
		setProperty(WSHandlerConstants.ACTION, WSHandlerConstants.SAML_TOKEN_SIGNED);
	}

	@Override
	public Crypto loadSignatureCrypto(RequestData requestData) throws WSSecurityException {

		Properties signatureProperties = new Properties();
		signatureProperties.setProperty("org.apache.wss4j.crypto.merlin.truststore.file", System.getProperty("javax.net.ssl.trustStore"));
		signatureProperties.setProperty("org.apache.wss4j.crypto.merlin.truststore.password", System.getProperty("javax.net.ssl.trustStorePassword"));

		return CryptoFactory.getInstance(signatureProperties);
	}

	@Override
	public void handleMessage(SoapMessage msg) {
		super.handleMessage(msg);

		if (!isPingCall(msg)) {
			SAMLSecurityContext sc = (SAMLSecurityContext) msg.get(SecurityContext.class.getName());
			if (sc == null) {
				throw new DokarkivTechnicalException("Cannot get SecurityContext from SoapMessage");
			}
			SAMLTokenPrincipal samlTokenPrincipal = (SAMLTokenPrincipal) sc.getUserPrincipal();
			if (samlTokenPrincipal == null) {
				throw new DokarkivTechnicalException("Cannot get SAMLTokenPrincipal from SecurityContext");
			}
			((ThreadLocalSubjectHandler) SubjectHandler.getSubjectHandler()).setSubject(buildSubject(sc));
		}
	}

	private boolean isPingCall(SoapMessage msg) {
		return PING_ACTIONS.contains((String) msg.getOrDefault("SOAPAction", ""));
	}

	private Subject buildSubject(SAMLSecurityContext samlSecurityContext) {
		try {
			final SamlAssertionWrapper samlAssertionWrapper = new SamlAssertionWrapper(samlSecurityContext.getAssertionElement());
			Map<String, String> attributeValues = extractSamlAttributeValues(samlAssertionWrapper);
			final IdentType identType = IdentType.valueOf(attributeValues.getOrDefault("identType", IdentType.Prosess.name()));
			final int authenticationLevel = Integer.parseInt(attributeValues.getOrDefault("authenticationLevel", "0"));
			final String consumerId = attributeValues.getOrDefault("consumerId", "unknown");
			final Subject subject = new Subject();
			subject.getPrincipals().add(new SluttBruker(samlAssertionWrapper.getSubjectName(), identType));
			subject.getPublicCredentials().add(new AuthenticationLevelCredential(authenticationLevel));
			subject.getPublicCredentials().add(new SAMLAssertionCredential(samlAssertionWrapper.getElement()));
			subject.getPrincipals().add(new ConsumerId(consumerId));
			return subject;
		} catch (WSSecurityException e) {
			throw new ApplicationException("Unable to extract SAML assertion", e);
		}
	}

	private Map<String, String> extractSamlAttributeValues(SamlAssertionWrapper samlAssertionWrapper) {
		return samlAssertionWrapper.getSaml2().getAttributeStatements().stream()
				.flatMap(attributeStatement -> attributeStatement.getAttributes().stream())
				.collect(Collectors.toMap(Attribute::getName, attribute -> {
					final XMLObject xmlObject = attribute.getAttributeValues().get(0);
					if (xmlObject instanceof XSString) {
						return ((XSString) xmlObject).getValue();
					} else if (xmlObject instanceof XSAnyImpl) {
						return ((XSAnyImpl) xmlObject).getTextContent();
					} else {
						return "unknown";
					}
				}));
	}
}