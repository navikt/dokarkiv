package no.nav.dokarkiv.core.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

@Slf4j
public class ValidateAdminConsumerAccessInterceptor implements HandlerInterceptor {

	public static final String APP_NAME_WITH_NAMESPACE = "teamdokumenthandtering:joarkadmin";

	private final JoarkVedlikeholdInterceptor joarkVedlikeholdInterceptor;

	public ValidateAdminConsumerAccessInterceptor(AzureAdGraphService azureAdGraphService,
												  String joarkVedlikeholdGroupObjectId) {
		this.joarkVedlikeholdInterceptor = new JoarkVedlikeholdInterceptor(azureAdGraphService, joarkVedlikeholdGroupObjectId);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		// To automatiske jobber på joarkadmin kaller admin-endepunktene med client credential token
		if (erIkkeEtOboToken()) {
			if (erClientCredentialToken()) {
				return true;
			} else {
				log.warn("OIDC-token på Authorization-header inneholder et system-til-system-token som ikke tilhører {}", APP_NAME_WITH_NAMESPACE);

				response.sendError(SC_UNAUTHORIZED, "OIDC-token på Authorization-header må være et client credential token som tilhører " + APP_NAME_WITH_NAMESPACE);
				return false;
			}
		}

		return joarkVedlikeholdInterceptor.preHandle(request, response, handler);
	}

	private boolean erClientCredentialToken() {
		return MDC.get(MDC_CONSUMER_ID).contains(APP_NAME_WITH_NAMESPACE);
	}

	private boolean erIkkeEtOboToken() {
		return MDC.get(MDC_USER_ID).equals(MDC.get(MDC_CONSUMER_ID));
	}

}