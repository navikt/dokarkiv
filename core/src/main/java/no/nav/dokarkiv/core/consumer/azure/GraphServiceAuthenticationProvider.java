package no.nav.dokarkiv.core.consumer.azure;

import com.microsoft.kiota.RequestInformation;
import com.microsoft.kiota.ResponseHandlerOption;
import com.microsoft.kiota.authentication.AuthenticationProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

import static no.nav.dokarkiv.core.NavHeaders.BEARER_TOKEN_PREFIX;
import static no.nav.dokarkiv.core.consumer.azure.AzureToken.isOnBehalfOfAzureToken;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class GraphServiceAuthenticationProvider implements AuthenticationProvider {

	private static final String MICROSOFT_GRAPH_SCOPE = "https://graph.microsoft.com/.default";
	private final AzureToken azureToken;

	public GraphServiceAuthenticationProvider(AzureToken azureToken) {
		this.azureToken = azureToken;
	}

	@Override
	public void authenticateRequest(RequestInformation request, Map<String, Object> additionalAuthenticationContext) {
		String token = accessTokenFromRequest(request);
		if (isNotBlank(token) && isOnBehalfOfAzureToken(token)) {
			request.headers.add("Authorization", "Bearer " + azureToken.onBehalfOfAccessToken(token, MICROSOFT_GRAPH_SCOPE));
		}
		request.headers.add("Authorization", "Bearer " + azureToken.clientCredentialAccessToken(MICROSOFT_GRAPH_SCOPE));
	}

	private String accessTokenFromRequest(RequestInformation request) {
		return request.headers.get(AUTHORIZATION).stream()
				.filter(h -> h.startsWith(BEARER_TOKEN_PREFIX))
				.map(e -> e.replaceFirst(BEARER_TOKEN_PREFIX, ""))
				.findAny().orElse(null);
	}
}
