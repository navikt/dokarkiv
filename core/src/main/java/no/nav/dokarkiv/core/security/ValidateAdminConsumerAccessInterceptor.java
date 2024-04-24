package no.nav.dokarkiv.core.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
public class ValidateAdminConsumerAccessInterceptor implements HandlerInterceptor {

	private static final String ADMIN_SERVICE_USER = "srvjoarkadmin";

	private final HeaderTokenExtractor headerTokenExtractor = new HeaderTokenExtractor();
	private final AzureAdGraphService azureAdGraphService;
	private final String adminServiceUserAdRole;

	public ValidateAdminConsumerAccessInterceptor(AzureAdGraphService azureAdGraphService, String adminServiceUserAdRole) {
		this.azureAdGraphService = azureAdGraphService;
		this.adminServiceUserAdRole = adminServiceUserAdRole;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		if (response.getStatus() != OK.value()) {
			//This means that the validation of oidc tokens failed in IdTokenAuthenticationFilter and we should let the handler go through
			return true;
		}

		String navConsumerToken = headerTokenExtractor.getConsumerToken(request);
		String authorizationToken = headerTokenExtractor.getIdToken(request);

		if (isEmpty(navConsumerToken)) {
			if (isFalse(isTokenBelongsToUser(authorizationToken, ADMIN_SERVICE_USER))) {
				String message = format("OIDC token på Authorization-header må tilhøre servicebruker på %s", ADMIN_SERVICE_USER);
				log.warn(message);
				response.sendError(SC_UNAUTHORIZED, message);
				return false;
			}
		} else if (isNotEmpty(navConsumerToken)) {
			if (isFalse(isTokenBelongsToUser(navConsumerToken, ADMIN_SERVICE_USER))) {
				String message = format("OIDC token på Nav-Consumer-Token header må tilhøre serviceuser på %s", ADMIN_SERVICE_USER);
				log.warn(message);
				response.sendError(SC_UNAUTHORIZED, message);
				return false;
			} else if (isFalse(isUserInTokenHasRole(authorizationToken, adminServiceUserAdRole))) {
				String message = format("NAVIdent må være medlem av gruppen guid=\"%s\" i Azure AD", adminServiceUserAdRole);
				log.error(message);
				response.sendError(SC_UNAUTHORIZED, message);
				return false;
			}
		} else {
			String message = "Token header må være satt";
			log.warn(message);
			response.sendError(SC_UNAUTHORIZED, message);
			return false;
		}
		return true;
	}

	public boolean isUserInTokenHasRole(String token, String ldapGroup) {
		String userId = getSubjectFromToken(token);
		return azureAdGraphService.userInGroup(userId, ldapGroup);
	}

	public boolean isTokenBelongsToUser(String token, String subject) {
		if (isNotEmpty(token)) {
			String consumerID = getSubjectFromToken(token);
			return subject.equalsIgnoreCase(consumerID) || azureClaimWorkaround(token);
		}
		return false;
	}

	private boolean azureClaimWorkaround(String token) {
		DecodedJWT decode = JWT.decode(token);
		String azpName = decode.getClaim("azp_name").asString();
		if (azpName == null) {
			return false;
		} else {
			return azpName.contains("joarkadmin");
		}
	}

	private String getSubjectFromToken(String token) {
		if (isEmpty(token)) {
			return null;
		}
		return JWT.decode(token).getSubject();
	}
}
