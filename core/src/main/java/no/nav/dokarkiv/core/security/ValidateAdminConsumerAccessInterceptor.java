package no.nav.dokarkiv.core.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
public class ValidateAdminConsumerAccessInterceptor implements HandlerInterceptor {

	private static final List<String> VALID_CALLERS = List.of("joarkadmin");
	private static final String AUTHORIZED_PARTY_NAME_CLAIM = "azp_name";
	private static final String OID_CLAIM = "oid";

	private final HeaderTokenExtractor headerTokenExtractor = new HeaderTokenExtractor();
	private final AzureAdGraphService azureAdGraphService;
	private final String joarkVedlikeholdAdGruppeId;

	public ValidateAdminConsumerAccessInterceptor(AzureAdGraphService azureAdGraphService, String joarkVedlikeholdAdGruppeId) {
		this.azureAdGraphService = azureAdGraphService;
		this.joarkVedlikeholdAdGruppeId = joarkVedlikeholdAdGruppeId;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		if (response.getStatus() != OK.value()) {
			//This means that the validation of oidc tokens failed in IdTokenAuthenticationFilter and we should let the handler go through
			return true;
		}

		String authorizationToken = headerTokenExtractor.getIdToken(request);
		if (authorizationToken == null) {
			log.warn("Kall mot admin-endepunkt mangler Authorization-header");

			response.sendError(SC_UNAUTHORIZED, "Authorization-header må være satt");
			return false;
		}

		if (!consumerAppIsJoarkadmin(authorizationToken)) {
			log.warn(format("OIDC-token på Authorization-header tilhører ikke en av følgende apper=%s", VALID_CALLERS));

			response.sendError(SC_UNAUTHORIZED, format("OIDC-token på Authorization-header må tilhøre en av følgende apper=%s", VALID_CALLERS));
			return false;
		}

		if (!userHasRequiredRole(authorizationToken, joarkVedlikeholdAdGruppeId)) {
			log.error(format("NAVIdent er ikke medlem av gruppen guid=\"%s\" i Azure AD", joarkVedlikeholdAdGruppeId));

			response.sendError(SC_UNAUTHORIZED, format("NAVIdent må være medlem av gruppen guid=\"%s\" i Azure AD", joarkVedlikeholdAdGruppeId));
			return false;
		}

		return true;
	}

	private static boolean consumerAppIsJoarkadmin(String token) {
		DecodedJWT decode = JWT.decode(token);
		String azpName = decode.getClaim(AUTHORIZED_PARTY_NAME_CLAIM).asString();

		if (azpName == null) {
			return false;
		}

		return VALID_CALLERS.stream().anyMatch(azpName::contains);
	}

	public boolean userHasRequiredRole(String token, String azureAdGroup) {
		DecodedJWT decode = JWT.decode(token);
		String oid = decode.getClaim(OID_CLAIM).asString();

		return azureAdGraphService.userIsMemberOfGroup(oid, azureAdGroup);
	}

}