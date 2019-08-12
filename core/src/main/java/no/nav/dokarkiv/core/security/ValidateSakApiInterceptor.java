package no.nav.dokarkiv.core.security;

import static no.nav.dokarkiv.core.util.DecodeUtils.decodeBasicAuth;
import static no.nav.freg.security.oidc.auth.OidcConstants.BEARER_TOKEN_PREFIX;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.auth0.jwt.JWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.jaxws.ThreadLocalSubjectHandler;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.dokarkiv.core.security.saml.SAMLValidator;
import no.nav.dokarkiv.core.util.ErrorResponse;
import no.nav.dokarkiv.core.util.JsonSerializer;
import no.nav.freg.security.oidc.auth.common.OidcTokenAuthentication;
import no.nav.freg.security.oidc.auth.idtoken.extract.HeaderTokenExtractor;
import no.nav.freg.security.oidc.auth.idtoken.strategy.IdTokenValidationStrategy;
import no.nav.freg.security.oidc.idp.validation.ValidationResult;
import no.nav.modig.core.context.SubjectHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.boot.system.SystemProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

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

	private final SAMLValidator samlValidator = new SAMLValidator(
			SystemProperties.get("javax.net.ssl.trustStore"),
			SystemProperties.get("javax.net.ssl.trustStorePassword"));

	private static final String SAML_TOKEN_PREFIX = "Saml ";
	private static final String BASIC_TOKEN_PREFIX = "Basic ";
	private static final String CORRELATION_HEADER = "X-Correlation-ID";
	private static final String UUID_HEADER = "X-UUID";
	private static final String UKJENT = "UKJENT";

	private final HeaderTokenExtractor headerTokenExtractor = new HeaderTokenExtractor();

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		((ThreadLocalSubjectHandler) SubjectHandler.getSubjectHandler()).reset();

		String correlationId = request.getHeader(CORRELATION_HEADER);
		MDC.put(MDCConstants.MDC_CALL_ID, UUID.randomUUID().toString());

		if (StringUtils.isBlank(correlationId)) {
			log.warn("Forventet følgende header: {}, avbryter forespørsel", CORRELATION_HEADER);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON.getMimeType());
			response.getWriter()
					.print(JsonSerializer
							.serialize(ErrorResponse.builder()
									.feilmelding(String.format("Påkrevd header mangler: %s", CORRELATION_HEADER))
									.uuid(MDC.get(MDCConstants.MDC_CALL_ID))
									.build()));
			return false;
		}

		MDC.put(MDCConstants.MDC_CORRELATION_ID, correlationId);

		response.setHeader(CORRELATION_HEADER, MDC.get(MDCConstants.MDC_CORRELATION_ID));
		response.setHeader(UUID_HEADER, MDC.get(MDCConstants.MDC_CALL_ID));

		putAbacMdcValues(request);

		AuthenticationResult result = AuthenticationResult.builder().build();
		if (isOIDCToken(request)) {
			result = authorizeOIDCToken(request, response);
		} else if (isSAMLToken(request)) {
			result = authorizeSAMLToken(request);
		} else if (isBasicAuth(request)) {
			result = authorizeBasicAuth(request);
		}

		if (!result.isValid()) {
			String message = "Autentisering feilet, se kibana for årsak";
			log.warn("Autentisering feilet: " + result.getErrorMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON.getMimeType());
			response.getWriter()
					.print(JsonSerializer
							.serialize(ErrorResponse.builder().feilmelding(message).uuid(MDC.get(MDCConstants.MDC_CALL_ID)).build()));
			return false;
		}

		return true;

	}

	private AuthenticationResult authorizeSAMLToken(HttpServletRequest request) {
		String samlTokenBase64 = getSamlToken(request);
		AuthenticationResult authenticationResult = samlValidator.validate(samlTokenBase64);

		if (authenticationResult.isValid()) {
			MDC.put(MDCConstants.MDC_CONSUMER_ID, authenticationResult.getConsumerId());
			MDC.put(MDCConstants.MDC_USER_ID, authenticationResult.getUser());
		}

		return authenticationResult;
	}

	private AuthenticationResult authorizeBasicAuth(HttpServletRequest request) {
		String token = getBasicAuthToken(request);
		String[] basicAuthToken = decodeBasicAuth(token);
		String username = basicAuthToken[0];
		String ***passord=gammelt_passord***];

		AuthenticationResult authenticationResult = navLdapService.authenticateLdapUser(username, password);
		if (!authenticationResult.isValid()) {
			return AuthenticationResult.builder().isValid(false).errorMessage(authenticationResult.getErrorMessage()).build();
		}

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(username, password));
		return AuthenticationResult.builder().isValid(true).build();


	}

	private AuthenticationResult authorizeOIDCToken(HttpServletRequest request, HttpServletResponse response) {

		try {
			ValidationResult validationResult = validationStrategy.validate(request, response);
			String authorizationToken = headerTokenExtractor.getIdToken(request);
			Authentication auth = authManager.authenticate(new OidcTokenAuthentication(authorizationToken, null, validationResult));
			SecurityContextHolder.getContext().setAuthentication(auth);

			String userName = getSubjectFromToken(authorizationToken);
			String audience = getAudienceFromOidcToken(authorizationToken);
			MDC.put(MDCConstants.MDC_CONSUMER_ID, audience);
			MDC.put(MDCConstants.MDC_USER_ID, userName);
			return AuthenticationResult.builder().isValid(true).build();

		} catch (Exception e) {
			return AuthenticationResult.builder().isValid(false).errorMessage(e.getMessage()).build();
		}
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

	private String getAudienceFromOidcToken(String token) {
		if (isEmpty(token)) {
			return null;
		}
		return JWT.decode(token).getAudience().stream().findFirst().orElse(UKJENT);
	}

	private String getSamlToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(AUTHORIZATION))
				.filter(e -> e.startsWith(SAML_TOKEN_PREFIX))
				.map(e -> e.replaceFirst(SAML_TOKEN_PREFIX, ""))
				.orElse("");
	}

	private String getBasicAuthToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(AUTHORIZATION))
				.filter(e -> e.startsWith(BASIC_TOKEN_PREFIX))
				.orElse("");
	}



}
