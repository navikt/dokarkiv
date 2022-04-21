package no.nav.dokarkiv.core;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokarkiv.core.jaxws.ThreadLocalSubjectHandler;
import no.nav.dokarkiv.core.metrics.DokTimedAspect;
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
import org.springframework.retry.annotation.EnableRetry;

import javax.annotation.PostConstruct;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ComponentScan
@Configuration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
@EnableConfigurationProperties({ServiceuserAlias.class, DokarkivProperties.class})
@EnableAspectJAutoProxy
@EnableRetry
public class CoreConfig {

	@Bean
	public DokTimedAspect timedAspect(MeterRegistry meterRegistry) {
		return new DokTimedAspect(meterRegistry);
	}

	@PostConstruct
	public void postConstruct() {
		setProperty(SUBJECTHANDLER_KEY, ThreadLocalSubjectHandler.class.getName());
	}
}
