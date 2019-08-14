package no.nav.dokarkiv.core.security.saml;

import lombok.experimental.UtilityClass;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.modig.core.context.AuthenticationLevelCredential;
import no.nav.modig.core.context.SAMLAssertionCredential;
import no.nav.modig.core.domain.ConsumerId;
import no.nav.modig.core.domain.IdentType;
import no.nav.modig.core.domain.SluttBruker;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.saml.saml2.core.Attribute;
import org.w3c.dom.Element;

import javax.security.auth.Subject;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@UtilityClass
public class SamlUtils {


	public static Subject buildSubject(Element element) {
		try {
			final SamlAssertionWrapper samlAssertionWrapper = new SamlAssertionWrapper(element);
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


	public static Map<String, String> extractSamlAttributeValues(SamlAssertionWrapper samlAssertionWrapper) {
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
				}, (name1, name2) -> name1));
	}
}
