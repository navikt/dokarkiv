package no.nav.dokarkiv.core.security;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import no.nav.dokarkiv.core.properties.DokarkivProperties;
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@EnableJwtTokenValidation(ignore = {"org.springframework", "org.springdoc"})
@Configuration
public class RestWebMvcConfig implements WebMvcConfigurer {

	private final TokenValidationContextHolder tokenValidationContextHolder;
	private final MultiIssuerConfiguration multiIssuerConfiguration;
	private final AzureAdGraphService azureAdGraphService;
	private final HandlerInterceptor basicAuthReadAccessRestInterceptor;
	private final MeterRegistry meterRegistry;
	private final DokarkivProperties dokarkivProperties;


	public RestWebMvcConfig(TokenValidationContextHolder tokenValidationContextHolder,
							MultiIssuerConfiguration multiIssuerConfiguration,
							AzureAdGraphService azureAdGraphService,
							DokarkivProperties dokarkivProperties,
							@Lazy HandlerInterceptor basicAuthReadAccessRestInterceptor,
							MeterRegistry meterRegistry) {
		this.tokenValidationContextHolder = tokenValidationContextHolder;
		this.multiIssuerConfiguration = multiIssuerConfiguration;
		this.azureAdGraphService = azureAdGraphService;
		this.basicAuthReadAccessRestInterceptor = basicAuthReadAccessRestInterceptor;
		this.meterRegistry = meterRegistry;
		this.dokarkivProperties = dokarkivProperties;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {

		registry.addInterceptor(new ClearMDCHandler())
				.addPathPatterns("/rest/**", "/hentjournalsakinfo/**");

		registry.addInterceptor(basicAuthReadAccessRestInterceptor)
				.addPathPatterns("/hentjournalsakinfo/**");

		registry.addInterceptor(new SporingHandlerInterceptor(tokenValidationContextHolder, multiIssuerConfiguration, meterRegistry, azureAdGraphService))
				.addPathPatterns("/rest/**");

		registry.addInterceptor(new ValidateAdminConsumerAccessInterceptor(dokarkivProperties.getJoarkVedlikeholdGroupId()))
				.addPathPatterns("/rest/admin/**");

		registry.addInterceptor(new JoarkVedlikeholdTokenClaimOnlyInterceptor(dokarkivProperties.getJoarkVedlikeholdGroupId()))
				.addPathPatterns(
						"/rest/journalpostapi/v1/journalpost/*/feilregistrer/settUkjentBruker",
						"/rest/journalpostapi/v1/journalpost/*/feilregistrer/settStatusUtgår",
						"/rest/journalpostapi/v1/journalpost/*/endreJournalstatus");

		registry.addInterceptor(new PopulateMDCHandler())
				.addPathPatterns("/rest/**", "/hentjournalsakinfo/**");
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		// Fjernes etter at klienter er oppdatert
		configurer.setUseTrailingSlashMatch(true);
	}

	@Override
	public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
		LoggingExceptionResolver loggingExceptionResolver = new LoggingExceptionResolver();
		loggingExceptionResolver.setOrder(HIGHEST_PRECEDENCE + 1);
		resolvers.add(loggingExceptionResolver);
		AnnotationAwareOrderComparator.sort(resolvers);
	}
}
