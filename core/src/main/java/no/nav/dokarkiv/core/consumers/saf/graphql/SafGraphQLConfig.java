package no.nav.dokarkiv.core.consumers.saf.graphql;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

import javax.validation.constraints.NotEmpty;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ConfigurationProperties("saf")
@Configuration
@Validated
@Data
public class SafGraphQLConfig {

	@NotEmpty
	private String graphQlUrl;

	@NotEmpty
	private String scope;

	@Bean("safGraphQLClient")
	public WebClient webClient(WebClient.Builder webClientBuilder) {
		return webClientBuilder
				.clone()
				.baseUrl(graphQlUrl)
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}
}
