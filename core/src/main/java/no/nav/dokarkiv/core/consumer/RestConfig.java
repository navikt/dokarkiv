package no.nav.dokarkiv.core.consumer;

import no.nav.dokarkiv.core.properties.ServiceuserAlias;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

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
                .basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Bean
    ClientHttpRequestFactory requestFactory(HttpClient httpClient) {
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    @Bean
    HttpClient httpClient() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(400);
        connectionManager.setDefaultMaxPerRoute(100);
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
    }
}
