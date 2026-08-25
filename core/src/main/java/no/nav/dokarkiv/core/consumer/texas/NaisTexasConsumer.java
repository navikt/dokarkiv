package no.nav.dokarkiv.core.consumer.texas;

import no.nav.dokarkiv.core.NaisProperties;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class NaisTexasConsumer {

	private static final Pattern TARGET_PATTERN = Pattern.compile("api://[^.]+\\.[^.]+\\.[^.]+/\\.default");
	private final RestClient restClient;
	private final NaisProperties naisProperties;
	private final Set<String> approvedScopes;

	public NaisTexasConsumer(RestClient.Builder restClientBuilder, NaisProperties naisProperties) {
		this.restClient = restClientBuilder.build();
		this.naisProperties = naisProperties;
		this.approvedScopes = new HashSet<>();
	}

	/// Utveksle et TokenX token for å sende request til et annet system, vha Texas
	///
	/// @param targetScope Maskin man vil autorisere mot på format `<cluster>:<namespace>:<other-api-app-name>`
	/// @return Bearer token
	public String exchangeForTokenX(@NonNull String accessToken, @NonNull String targetScope) {
		if (isBlank(accessToken)) {
			throw new IllegalArgumentException("accessToken må være satt");
		}
		if (isBlank(targetScope)) {
			throw new IllegalArgumentException("targetScope må være satt");
		}

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("identity_provider", "tokenx");
		formData.add("target", targetScope);
		formData.add("user_token", accessToken);

		return requireNonNull(restClient
				.post()
				.uri(naisProperties.getTokenExchangeEndpoint())
				.accept(APPLICATION_JSON)
				.contentType(APPLICATION_FORM_URLENCODED)
				.body(formData)
				.retrieve()
				.body(NaisTexasToken.class))
				.accessToken();
	}

	/// Maskin-til-maskin systemtoken fra Texas
	///
	/// Bruker Entra ID
	///
	/// @param targetScope Maskin man vil autorisere mot på format `api://<cluster>.<namespace>.<other-api-app-name>/.default`
	/// @return Bearer token
	public String getSystemToken(@NonNull String targetScope) {
		if (!approvedScopes.contains(targetScope)) {
			if (isBlank(targetScope) || !TARGET_PATTERN.matcher(targetScope).matches()) {
				throw new IllegalArgumentException("Ugyldig targetScope. Må være på format api://<cluster>.<namespace>.<other-api-app-name>/.default");
			}
			approvedScopes.add(targetScope);
		}

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("identity_provider", "entra_id");
		formData.add("target", targetScope);

		return requireNonNull(restClient.post()
				.uri(naisProperties.getTokenEndpoint())
				.contentType(APPLICATION_FORM_URLENCODED)
				.body(formData)
				.retrieve()
				.body(NaisTexasToken.class))
				.accessToken();
	}

}