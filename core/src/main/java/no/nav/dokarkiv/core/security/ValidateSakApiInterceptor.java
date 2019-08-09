package no.nav.dokarkiv.core.security;

import static no.nav.freg.security.oidc.auth.OidcConstants.BEARER_TOKEN_PREFIX;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.auth0.jwt.JWT;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.jaxws.ThreadLocalSubjectHandler;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.dokarkiv.core.security.ldap.NavUser;
import no.nav.freg.security.oidc.auth.common.OidcTokenAuthentication;
import no.nav.freg.security.oidc.auth.idtoken.extract.HeaderTokenExtractor;
import no.nav.freg.security.oidc.auth.idtoken.strategy.IdTokenValidationStrategy;
import no.nav.freg.security.oidc.idp.validation.ValidationResult;
import no.nav.modig.core.context.AuthenticationLevelCredential;
import no.nav.modig.core.context.SAMLAssertionCredential;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.core.domain.ConsumerId;
import no.nav.modig.core.domain.IdentType;
import no.nav.modig.core.domain.SluttBruker;
import org.apache.cxf.rt.security.claims.Claim;
import org.apache.cxf.rt.security.saml.utils.SAMLUtils;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.saml.saml2.core.Attribute;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
public class ValidateSakApiInterceptor implements HandlerInterceptor {

	private final NavLdapService navLdapService;
	private final IdTokenValidationStrategy validationStrategy;
	private final AuthenticationManager authManager;

	private static final String UKJENT = "UKJENT";
	private static final String SAML_TOKEN_PREFIX = "Saml ";
	private static final String BASIC_TOKEN_PREFIX = "Basic ";
	private final HeaderTokenExtractor headerTokenExtractor = new HeaderTokenExtractor();

	@Data
	@Builder
	private static class AuthResult {
		private boolean isAuthorized;
		private String errorMessage;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		((ThreadLocalSubjectHandler) SubjectHandler.getSubjectHandler()).reset();

		if (response.getStatus() != HttpStatus.OK.value()) {
			//This means that the validation of oidc tokens failed in IdTokenAuthenticationFilter and we should let the handler go through
			return true;
		}

		AuthResult result = AuthResult.builder().build();
		if (isOIDCToken(request)) {
			result = authorizeOIDCToken(request, response);
		} else if (isSAMLToken(request)) {
			result = authorizeSAMLToken(request);
		} else if (isBasicAuth(request)) {
			result = authorizeBasicAuth(request);
		}

		if (!result.isAuthorized()) {
			String message = result.getErrorMessage();
			log.warn(message);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
			return false;

		}

		return true;

	}

	private AuthResult authorizeSAMLToken(HttpServletRequest request) {
		String samlTokenBase64 = getSamlToken(request);
		String samlToken = decodeSamlToken(samlTokenBase64);
		String consumerId = getConsumerFromSamlToken(samlToken);

		MDC.put(MDCConstants.MDC_CONSUMER_ID, consumerId);
		MDC.put(MDCConstants.MDC_USER_ID, consumerId);
		MDC.put(MDCConstants.MDC_USER_NAME, consumerId);
		((ThreadLocalSubjectHandler) SubjectHandler.getSubjectHandler()).setSubject(buildSubject(samlTokenToElement(samlToken)));
		return AuthResult.builder().isAuthorized(true).build();
	}

	private Subject buildSubject(Element element) {
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
				}, (name1, name2) -> name1));
	}

	private AuthResult authorizeBasicAuth(HttpServletRequest request) {
		return AuthResult.builder().isAuthorized(false).errorMessage("Basic auth is not supported").build();
	}

	private AuthResult authorizeOIDCToken(HttpServletRequest request, HttpServletResponse response) {
		ValidationResult validationResult = validationStrategy.validate(request, response);
		String authorizationToken = headerTokenExtractor.getIdToken(request);
		Authentication auth = authManager.authenticate(new OidcTokenAuthentication(authorizationToken, null, validationResult));
		SecurityContextHolder.getContext().setAuthentication(auth);

		String userName = getSubjectFromToken(authorizationToken);
		NavUser navUser = navLdapService.findByUserId(userName);
		NavUser navServiceUser = navLdapService.findByServiceuserId(userName);
		if (navUser.isUserExistsInLdap() || navServiceUser.isUserExistsInLdap()) {
			MDC.put(MDCConstants.MDC_CONSUMER_ID, userName);
			MDC.put(MDCConstants.MDC_USER_ID, userName);
			MDC.put(MDCConstants.MDC_USER_NAME, userName);
			return AuthResult.builder().isAuthorized(true).build();
		}

		return AuthResult.builder().isAuthorized(false).errorMessage("").build();

	}

	private boolean isBasicAuth(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(AUTHORIZATION))
				.filter(header -> header.startsWith(BASIC_TOKEN_PREFIX)).isPresent();
	}

	private boolean isOIDCToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(AUTHORIZATION))
				.filter(header -> header.startsWith(BEARER_TOKEN_PREFIX)).isPresent();
	}

	private boolean isSAMLToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(AUTHORIZATION))
				.filter(header -> header.startsWith(SAML_TOKEN_PREFIX)).isPresent();
	}

	private void putAbacMdcValues(HttpServletRequest request) {
		MDC.put(MDCConstants.MDC_HTTP_ENDPOINT, request.getRequestURL().toString());
		MDC.put(MDCConstants.MDC_HTTP_OPERATION, request.getMethod());
	}

	private String getSubjectFromToken(String token) {
		if (isEmpty(token)) {
			return null;
		}
		return JWT.decode(token).getSubject();
	}

	private String decodeSamlToken(String token) {

		byte[] base64Token = token.getBytes(StandardCharsets.UTF_8);
		byte[] decoded;

		try {
			decoded = Base64.getDecoder().decode(base64Token);
		} catch (IllegalArgumentException e) {
			log.warn("Kunne ikke dekode SAML authentication token", e);
			throw new BadCredentialsException(
					"Kunne ikke dekode SAML authentication token");
		}

		return new String(decoded, StandardCharsets.UTF_8);
	}

	private String getSamlToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(AUTHORIZATION))
				.filter(e -> e.startsWith(SAML_TOKEN_PREFIX))
				.map(e -> e.replaceFirst(SAML_TOKEN_PREFIX, ""))
				.orElse(null);
	}

	public static Element samlTokenToElement(String decodedSaml) {
		InputStream is = new ByteArrayInputStream(decodedSaml.getBytes());
		Document doc;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(is, StandardCharsets.UTF_8.name());
		} catch (ParserConfigurationException | IOException | SAXException e) {
			log.error(String.format("Feil ved parsing av SAML assertion token. Feilmelding=%s", e.getMessage()), e);
			throw new DokarkivTechnicalException("Feil ved parsing av SAML assertion token. Det kan hende tokenet er i feil format");
		}

		return doc.getDocumentElement();
	}

	public static SamlAssertionWrapper elementToSamlAssertionWrapper(Element token) {

		try {
			return new SamlAssertionWrapper(token);
		} catch (WSSecurityException e) {
			log.error(String.format("Feilet ved parsing av SAML assertion element til SamlAssertionWrapper. Feilmelding=%s", e.getMessage()), e);
			throw new DokarkivTechnicalException("Feilet ved parsing av SAML assertion token. Det kan hende tokenet er i feil format");
		}

	}

	private String getConsumerFromSamlToken(String decodedToken) {
		Element element = samlTokenToElement(decodedToken);
		String consumerId = null;

		try {
			consumerId = (String) SAMLUtils.getClaims(elementToSamlAssertionWrapper(element))
					.stream()
					.filter(claim -> claim.getClaimType().getPath().equalsIgnoreCase(MDCConstants.MDC_CONSUMER_ID))
					.findAny()
					.orElse(new Claim())
					.getValues()
					.get(0);
		} catch (Exception e) {
			//Do nothing
			log.warn("Feil ved henting av consumerId fra SAML token", e);
		}

		if (consumerId == null) {
			return UKJENT;
		}

		return consumerId;
	}
}
