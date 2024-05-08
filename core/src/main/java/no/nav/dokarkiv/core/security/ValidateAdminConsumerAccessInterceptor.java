package no.nav.dokarkiv.core.security;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

@Slf4j
public class ValidateAdminConsumerAccessInterceptor implements HandlerInterceptor {

	private static final String SERVICEBRUKER_JOARKADMIN = "srvjoarkadmin";

	private final JoarkVedlikeholdInterceptor joarkVedlikeholdInterceptor;

	public ValidateAdminConsumerAccessInterceptor(AzureAdGraphService azureAdGraphService,
												  String joarkVedlikeholdGroupObjectId) {
		this.joarkVedlikeholdInterceptor = new JoarkVedlikeholdInterceptor(azureAdGraphService, joarkVedlikeholdGroupObjectId);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		// To automatiske jobber på joarkadmin kaller admin-endepunktene med STS-token
		if (erIkkeEtOboToken()) {
			if (erStsTokenFraJoarkadmin()) {
				return true;
			} else {
				log.warn("OIDC-token på Authorization-header inneholder et system-til-system-token som ikke tilhører servicebruker={}", SERVICEBRUKER_JOARKADMIN);

				response.sendError(SC_UNAUTHORIZED, format("OIDC-token på Authorization-header må være et STS-token som tilhører servicebruker=%s", SERVICEBRUKER_JOARKADMIN));
				return false;
			}
		}

		return joarkVedlikeholdInterceptor.preHandle(request, response, handler);
	}

	private boolean erStsTokenFraJoarkadmin() {
		return SERVICEBRUKER_JOARKADMIN.equals(MDC.get(MDC_CONSUMER_ID));
	}

	private boolean erIkkeEtOboToken() {
		return MDC.get(MDC_USER_ID).equals(MDC.get(MDC_CONSUMER_ID));
	}

}