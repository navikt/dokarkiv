package no.nav.dokarkiv.core.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Common Spring configuration class for MOD services.
 *
 * @author Rune Romundstad, Visma Consulting
 */
@Configuration
//FIXME: SecurityConfig was removed, put it back!
//@EnableMetrics(proxyTargetClass = true)
@Import({DozerConfig.class})
public class BaseProviderConfig {

//	@Bean
//	public AbacSecurityService securityService() {
//		return new AbacSecurityService();
//	}
//
//	@Bean
//	public AbacLoggingUtils abacLoggingUtils() {
//		return new AbacLoggingUtils();
//	}

}
