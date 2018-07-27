package no.nav.dokarkiv.core;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.dokarkiv.core.fasit.ServiceuserAlias;
import no.nav.dokarkiv.core.jaxws.ThreadLocalSubjectHandler;
import no.nav.dokarkiv.core.metrics.DokTimedAspect;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;

import javax.annotation.PostConstruct;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ComponentScan
@Configuration
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(ServiceuserAlias.class)
@EnableAspectJAutoProxy
@EnableRetry
public class CoreConfig {
	@Bean
	public DokTimedAspect timedAspect(MeterRegistry meterRegistry) {
		return new DokTimedAspect(meterRegistry);
	}

	@PostConstruct
	public void postConstruct() {
		System.setProperty(ThreadLocalSubjectHandler.SUBJECTHANDLER_KEY, ThreadLocalSubjectHandler.class.getName());
	}
}
