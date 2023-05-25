package no.nav.dokarkiv.core.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.NavHeaders;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import no.nav.dokarkiv.core.security.handler.AzureAdFlowSporingHandler;
import no.nav.dokarkiv.core.security.handler.NavCombinedBrukerSystemkontekstHandler;
import no.nav.dokarkiv.core.security.handler.NavSystemkontekstHandler;
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static no.nav.dokarkiv.core.NavHeaders.NAV_CONSUMER_TOKEN;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Registerer sporing metadata for arkivering og endring etter arkivering.
 * Samt registrerer metrikker for alle journalpostapi kall.
 * <p>
 * Gjelder journalpostapi tjenestene.
 * <p>
 * MDC_USER_ID - systembruker eller saksbehandler ident
 * MDC_CONSUMER_ID - systembruker
 * MDC_USERNAME - systembruker eller saksbehandlers fulle navn
 * <p>
 * Blir brukt til å sette metadata på Journalpost treet:
 * * opprettet_kilde_navn
 * * endret_kilde_navn
 * * opprettet_av
 * * endret_av
 * * opprettet_av_navn
 * * endret_av_navn
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
public class SporingHandlerInterceptor implements HandlerInterceptor {

	private static final String UNKNOWN_VALUE = "unknown";
	public static final String ISSUER_AZUREV2 = "azurev2";
	public static final String ISSUER_RESTSTS = "reststs";
	private final MeterRegistry meterRegistry;
	private static final String UKJENT = "UKJENT";
	private final HeaderTokenExtractor headerTokenExtractor;
	private final AzureAdFlowSporingHandler azureAdFlowSporingHandler;
	private final NavSystemkontekstHandler navSystemkontekstHandler;
	private final NavCombinedBrukerSystemkontekstHandler navCombinedBrukerSystemkontekstHandler;
	private final TokenValidationContextHolder tokenValidationContextHolder;

	public SporingHandlerInterceptor(TokenValidationContextHolder tokenValidationContextHolder,
									 MultiIssuerConfiguration multiIssuerConfiguration,
									 MeterRegistry meterRegistry,
									 AzureAdGraphService azureAdGraphService) {
		this.tokenValidationContextHolder = tokenValidationContextHolder;
		this.meterRegistry = meterRegistry;
		this.headerTokenExtractor = new HeaderTokenExtractor();
		this.azureAdFlowSporingHandler = new AzureAdFlowSporingHandler(azureAdGraphService);
		this.navSystemkontekstHandler = new NavSystemkontekstHandler(azureAdGraphService);
		this.navCombinedBrukerSystemkontekstHandler = new NavCombinedBrukerSystemkontekstHandler(azureAdGraphService,
				multiIssuerConfiguration.getIssuer(ISSUER_RESTSTS).orElseThrow().getTokenValidator());
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		putHttpMdcValues(request);
		String authorizationToken = headerTokenExtractor.getIdToken(request);
		String navConsumerToken = headerTokenExtractor.getConsumerToken(request);
		String navUserIdHeader = request.getHeader(NavHeaders.NAV_USER_ID);

		if (isEmpty(authorizationToken)) {
			return handleMissingAuthorizationHeader(response);
		} else {
			return handleAuthorizedAccess(response, handler, authorizationToken, navConsumerToken, navUserIdHeader);
		}
	}

	private boolean handleAuthorizedAccess(HttpServletResponse response,
										   Object handler,
										   String authorizationToken,
										   String navConsumerToken,
										   String navUserIdHeader) throws IOException {
		final TokenValidationContext tokenValidationContext = tokenValidationContextHolder.getTokenValidationContext();
		if (tokenValidationContext.getJwtTokenAsOptional(ISSUER_AZUREV2).isPresent()) {
			// Azure AD token (header: Authorization). Oauth 2.0 client credential grant flow og on-behalf-of flow
			azureAdFlowSporingHandler.handle(tokenValidationContext.getJwtToken(ISSUER_AZUREV2), navUserIdHeader);
		} else if (tokenValidationContext.getFirstValidToken().isPresent() && isEmpty(navConsumerToken)) {
			// REST-STS (header: Authorization). System til system
			if (navSystemkontekstHandler.handle(tokenValidationContext.getFirstValidToken().get(), response, navUserIdHeader)) {
				return false;
			}
		} else if (tokenValidationContext.getFirstValidToken().isPresent() && isNotEmpty(navConsumerToken)) {
			// OpenAM (header: Authorization) og REST-STS on premise issuer (header: Nav-Consumer-Token).
			// Brukerkontekst i header Authorization.
			// Systemkontekst i header Nav-Consumer-Token.
			if (navCombinedBrukerSystemkontekstHandler.handle(tokenValidationContext.getFirstValidToken().get(), navConsumerToken, response)) {
				return false;
			}
		} else {
			return handleInvalidAuthorizationHeaderToken(response);
		}

		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			final String methodName = handlerMethod.getMethod().getName();
			final String controllerName = (handlerMethod.getMethod()).getDeclaringClass().getSimpleName();
			handleMetrics(methodName, controllerName, navConsumerToken, authorizationToken);
		} else {
			handleMetrics(UNKNOWN_VALUE, UNKNOWN_VALUE, navConsumerToken, authorizationToken);
		}

		return true;
	}

	private boolean handleMissingAuthorizationHeader(HttpServletResponse response) throws IOException {
		String message = "Authorization headeren mangler Bearer JWT. Undersøk om Authorization header har 'Bearer ' etterfulgt av en utstedt JWT.";
		return handleUnauthorizedAccess(response, message);
	}

	private boolean handleInvalidAuthorizationHeaderToken(HttpServletResponse response) throws IOException {
		String message = "Authorization headeren mangler gyldig Bearer JWT. Token kan ha timet ut eller være utstedt av issuer endepunktet ikke støtter.";
		return handleUnauthorizedAccess(response, message);
	}

	private boolean handleUnauthorizedAccess(HttpServletResponse response, final String message) throws IOException {
		log.warn(message);
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
		return false;
	}

	private void handleMetrics(final String methodName, final String controllerName, final String navConsumerToken, final String authorizationToken) {
		try {
			final DecodedJWT authorizationJWT = JWT.decode(authorizationToken);
			incrementAudienceCounter(HttpHeaders.AUTHORIZATION, authorizationJWT.getIssuer(), authorizationJWT.getAudience()
					.stream().findFirst().orElse(UNKNOWN_VALUE));
			if (isBlank(navConsumerToken)) {
				incrementConsumerCounter(authorizationJWT.getSubject(), methodName, controllerName);
			} else {
				final DecodedJWT navConsumerJWT = JWT.decode(navConsumerToken);
				incrementConsumerCounter(navConsumerJWT.getSubject(), methodName, controllerName);
				incrementAudienceCounter(NAV_CONSUMER_TOKEN, navConsumerJWT.getIssuer(), navConsumerJWT.getAudience()
						.stream().findFirst().orElse(UNKNOWN_VALUE));
			}
		} catch (Exception e) {
			log.warn("Det skjedde feil ved henting av consumer, metode eller controller navn for inkrementering av metrikker", e);
		}
	}

	private void putHttpMdcValues(HttpServletRequest request) {
		MDC.put(MDCConstants.MDC_HTTP_ENDPOINT, request.getRequestURL().toString());
		MDC.put(MDCConstants.MDC_HTTP_OPERATION, request.getMethod());
	}

	private void incrementConsumerCounter(String consumer, String methodName, String controllerName) {
		meterRegistry.counter("dok_request_consumer_name",
				"consumer_name", consumer == null ? UKJENT : consumer,
				"method_name", methodName == null ? UKJENT : methodName,
				"controller_name", controllerName == null ? UKJENT : controllerName).increment();
	}

	private void incrementAudienceCounter(final String header, final String issuer, final String audience) {
		Counter.builder("dok_request_audience")
				.tags("header", header)
				.tags("issuer", issuer)
				.tags("audience", audience)
				.register(meterRegistry)
				.increment();
	}
}
