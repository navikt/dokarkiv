package no.nav.dokarkiv.core.security.azure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.validation.constraints.NotEmpty;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Data
@ConfigurationProperties("azure")
@Configuration
@Validated
public class AzureConfig {

    @NotEmpty
    private String tokenUrl;
    @NotEmpty
    private String openidConfigTokenEndpoint;
    @NotEmpty
    private String appClientId;
    @NotEmpty
    private String appClientSecret;

    @Bean("azureClient")
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        HttpClient httpClient = HttpClient.create().proxyWithSystemProperties();
        return webClientBuilder
                .clone()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(openidConfigTokenEndpoint)
                .defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

}
