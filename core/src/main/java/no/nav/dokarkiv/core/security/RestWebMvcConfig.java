package no.nav.dokarkiv.core.security;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.inject.Named;

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

    public RestWebMvcConfig(TokenValidationContextHolder tokenValidationContextHolder,
                            MultiIssuerConfiguration multiIssuerConfiguration,
                            AzureAdGraphService azureAdGraphService,
                            @Lazy @Named("basicAuthReadAccessRestInterceptor") HandlerInterceptor basicAuthReadAccessRestInterceptor,
                            MeterRegistry meterRegistry) {
        this.tokenValidationContextHolder = tokenValidationContextHolder;
        this.multiIssuerConfiguration = multiIssuerConfiguration;
        this.azureAdGraphService = azureAdGraphService;
        this.basicAuthReadAccessRestInterceptor = basicAuthReadAccessRestInterceptor;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(basicAuthReadAccessRestInterceptor)
                .addPathPatterns("/hentjournalsakinfo/**");

        registry.addInterceptor(new SporingHandlerInterceptor(tokenValidationContextHolder, multiIssuerConfiguration, meterRegistry, azureAdGraphService))
                .excludePathPatterns("/rest/intern/**")
                .addPathPatterns("/rest/**");

        registry.addInterceptor(new ValidateAdminConsumerAccessInterceptor(azureAdGraphService))
                .addPathPatterns("/rest/admin/**");

        registry.addInterceptor(new PopulateMDCHandler())
                .addPathPatterns("/rest/**", "/hentjournalsakinfo/**");
    }
}
