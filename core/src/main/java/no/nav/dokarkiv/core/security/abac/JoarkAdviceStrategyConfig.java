package no.nav.dokarkiv.core.security.abac;

import no.nav.freg.abac.core.dto.response.Advice;
import no.nav.freg.abac.core.service.advice.AdviceStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

/**
 * @author Martin Burheim Tingstad, Visma Consulting AS
 */
@Configuration
public class JoarkAdviceStrategyConfig {

	static final String DENY_INFO = "no.nav.abac.advices.info.deny_info";

	@Inject
	private AbacLoggingUtils abaclog;

	@Bean
	AdviceStrategy joarkDefaultAdviceStrategy() {
		return new AdviceStrategy() {
			@Override
			public boolean isSupported(String s) {
				return true;
			}

			@Override
			public void perform(Advice advice) {
				abaclog.logAdvice(advice);
			}
		};
	}

}
