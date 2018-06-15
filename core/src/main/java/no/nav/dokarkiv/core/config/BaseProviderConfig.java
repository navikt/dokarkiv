package no.nav.dokarkiv.core.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Common Spring configuration class for MOD services.
 *
 * @author Rune Romundstad, Visma Consulting
 */
@Configuration
@EnableMetrics(proxyTargetClass = true)
@Import({DozerConfig.class, SecurityBaseConfig.class})
public class BaseProviderConfig {

	@Bean
	public AbacSecurityService securityService() {
		return new AbacSecurityService();
	}

	@Bean
	public AbacLoggingUtils abacLoggingUtils() {
		return new AbacLoggingUtils();
	}

}
