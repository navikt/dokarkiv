package no.nav.dokarkiv.core.security;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@EnableJwtTokenValidation(ignore = {"org.springframework", "org.springdoc"})
@Configuration
public class RestWebMvcConfig implements WebMvcConfigurer {

    private final TokenValidationContextHolder tokenValidationContextHolder;
    private final MultiIssuerConfiguration multiIssuerConfiguration;
    private final AzureAdGraphService azureAdGraphService;
    private final HandlerInterceptor basicAuthReadAccessRestInterceptor;
    private final MeterRegistry meterRegistry;
    private final String azureAdAdminRole;


    public RestWebMvcConfig(TokenValidationContextHolder tokenValidationContextHolder,
                            MultiIssuerConfiguration multiIssuerConfiguration,
                            AzureAdGraphService azureAdGraphService,
                            @Value("${azure.ad.admin.role}") String azureAdAdminRole,
                            @Lazy HandlerInterceptor basicAuthReadAccessRestInterceptor,
                            MeterRegistry meterRegistry) {
        this.tokenValidationContextHolder = tokenValidationContextHolder;
        this.multiIssuerConfiguration = multiIssuerConfiguration;
        this.azureAdGraphService = azureAdGraphService;
        this.basicAuthReadAccessRestInterceptor = basicAuthReadAccessRestInterceptor;
        this.meterRegistry = meterRegistry;
        this.azureAdAdminRole = azureAdAdminRole;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(basicAuthReadAccessRestInterceptor)
                .addPathPatterns("/hentjournalsakinfo/**");

        registry.addInterceptor(new SporingHandlerInterceptor(tokenValidationContextHolder, multiIssuerConfiguration, meterRegistry, azureAdGraphService))
                .addPathPatterns("/rest/**");

        registry.addInterceptor(new ValidateAdminConsumerAccessInterceptor(azureAdGraphService, azureAdAdminRole))
                .addPathPatterns("/rest/admin/**");

        registry.addInterceptor(new PopulateMDCHandler())
                .addPathPatterns("/rest/**", "/hentjournalsakinfo/**");
    }
}
