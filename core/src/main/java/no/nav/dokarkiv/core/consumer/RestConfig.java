package no.nav.dokarkiv.core.consumer;

import no.nav.dokarkiv.core.fasit.ServiceuserAlias;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
public class RestConfig {
	@Bean
	RestTemplate restTemplate(ServiceuserAlias serviceuserAlias,
							  RestTemplateBuilder restTemplateBuilder,
							  ClientHttpRequestFactory requestFactory) {
		return restTemplateBuilder
				.requestFactory(() -> requestFactory)
				.basicAuthorization(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.setConnectTimeout(5000)
				.setReadTimeout(5000).build();
	}

	@Bean
	ClientHttpRequestFactory requestFactory(HttpClient httpClient) {
		return new HttpComponentsClientHttpRequestFactory(httpClient);
	}

	@Bean
	HttpClient httpClient() {
		return HttpClients.createDefault();
	}
}
