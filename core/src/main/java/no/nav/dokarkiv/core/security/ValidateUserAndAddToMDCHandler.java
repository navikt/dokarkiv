package no.nav.dokarkiv.core.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.jaxws.ThreadLocalSubjectHandler;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.dokarkiv.core.security.ldap.NavUser;
import no.nav.modig.core.context.SubjectHandler;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
public class ValidateUserAndAddToMDCHandler implements HandlerInterceptor {

    private static final String UNKNOWN_VALUE = "unknown";
    private static final String NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
    private final NavLdapService navLdapService;
    private final MeterRegistry meterRegistry;
    private static final String UKJENT = "UKJENT";
    private final HeaderTokenExtractor headerTokenExtractor = new HeaderTokenExtractor();

    public ValidateUserAndAddToMDCHandler(NavLdapService navLdapService, MeterRegistry meterRegistry) {
        this.navLdapService = navLdapService;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        ((ThreadLocalSubjectHandler) SubjectHandler.getSubjectHandler()).reset();

        if (response.getStatus() != HttpStatus.OK.value()) {
            //This means that the validation of oidc tokens failed in IdTokenAuthenticationFilter and we should let the handler go through
            return true;
        }

        putAbacMdcValues(request);

        String navConsumerToken = headerTokenExtractor.getConsumerToken(request);
        String authorizationToken = headerTokenExtractor.getIdToken(request);

        if (isEmpty(authorizationToken)) {
            String message = "Finner ingen oidc token på Authorization header. Requesten må enten ha oidc-token for servicebruker på header med key=Authorization og value=Bearer [oidc-token] eller ha oidc-token for internbruker i Authorization header og servicebruker på header med key=Nav-Consumer-Token og value=Bearer [oidc-token]";
            log.warn(message);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
            return false;
        } else {
            if (isNotEmpty(authorizationToken) && isNotEmpty(navConsumerToken)) {
                String userId = getSubjectFromToken(authorizationToken);
                NavUser navUser = navLdapService.findByUserId(userId);
                if (navUser.isUserExistsInLdap()) {
                    MDC.put(MDCConstants.MDC_USER_ID, userId);
                    MDC.put(MDCConstants.MDC_USER_NAME, navUser.getFullname());
                } else {
                    String message = "OIDC token på Authorization header må tilhøre en Internbruker når både Authorization og Nav-Consumer-Token header er satt";
                    log.warn(message);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
                    return false;
                }

                String consumerID = getSubjectFromToken(navConsumerToken);
                NavUser consumer = navLdapService.findByServiceuserId(consumerID);
                if (consumer.isUserExistsInLdap()) {
                    MDC.put(MDCConstants.MDC_CONSUMER_ID, consumerID);
                } else {
                    String message = "OIDC token på Nav-Consumer-Token header må tilhøre en Servicebruker når både Authorization og Nav-Consumer-Token header er satt";
                    log.warn(message);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
                    return false;
                }
            } else {
                String consumerID = getSubjectFromToken(authorizationToken);
                NavUser consumer = navLdapService.findByServiceuserId(consumerID);
                if (consumer.isUserExistsInLdap()) {
                    MDC.put(MDCConstants.MDC_CONSUMER_ID, consumerID);
                    MDC.put(MDCConstants.MDC_USER_ID, consumerID);
                    MDC.put(MDCConstants.MDC_USER_NAME, consumerID);
                } else {
                    String message = "OIDC token på Authorization header må tilhøre en Servicebruker når Nav-Consumer-Token header ikke er satt";
                    log.warn(message);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
                    return false;
                }
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
