package no.nav.dokarkiv.core;

import no.nav.dokarkiv.core.consumer.texas.NaisTexasConsumer;
import no.nav.dokarkiv.core.consumer.texas.NaisTexasRequestInterceptor;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

	@Bean
	RestClient restClientTexas(RestClient.Builder restClientBuilder, NaisTexasConsumer naisTexasConsumer) {
		return restClientBuilder
				.requestFactory(jdkClientHttpRequestFactory())
				.requestInterceptor(new NaisTexasRequestInterceptor(naisTexasConsumer))
				.build();
	}

	private static JdkClientHttpRequestFactory jdkClientHttpRequestFactory() {
		return ClientHttpRequestFactoryBuilder.jdk()
				.withCustomizer(jdkClientHttpRequestFactory ->
						jdkClientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(20)))
				.build();
	}
}
