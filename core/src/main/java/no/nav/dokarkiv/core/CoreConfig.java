package no.nav.dokarkiv.core;

import no.nav.dokarkiv.core.properties.DokarkivProperties;
import no.nav.dokarkiv.core.properties.ServiceuserAlias;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.time.ZoneId;

@ComponentScan
@Configuration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
@EnableConfigurationProperties({ServiceuserAlias.class, DokarkivProperties.class})
@EnableAspectJAutoProxy
@EnableRetry
public class CoreConfig {

	public static final ZoneId ZONEID_NORGE = ZoneId.of("Europe/Oslo");

	@Bean
	WebClient webClient(WebClient.Builder webClientBuilder) {
		HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(60))
				.proxyWithSystemProperties();
		return webClientBuilder.clone()
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.build();
	}
}
