package no.nav.dokarkiv.core.consumer;

import no.nav.dokarkiv.core.properties.ServiceuserAlias;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

// FIXME
@Configuration
public class RestConfig {
    @Bean
    RestTemplate restTemplate(ServiceuserAlias serviceuserAlias,
                              RestTemplateBuilder restTemplateBuilder,
                              ClientHttpRequestFactory requestFactory) {
        return restTemplateBuilder
                .requestFactory(() -> requestFactory)
                .basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
                .build();
    }

    @Bean
    ClientHttpRequestFactory requestFactory(HttpClient httpClient) {
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    @Bean
    HttpClient httpClient() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(2))
                .build();
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(5))
                .build();

        connectionManager.setDefaultSocketConfig(socketConfig);
        connectionManager.setDefaultConnectionConfig(connectionConfig);
        connectionManager.setMaxTotal(400);
        connectionManager.setDefaultMaxPerRoute(100);
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
    }
}
