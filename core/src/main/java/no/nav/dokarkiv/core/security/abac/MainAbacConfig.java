package no.nav.dokarkiv.core.security.abac;

import no.nav.freg.abac.core.consumer.AbacConsumer;
import no.nav.freg.abac.spring.config.AbacConfig;
import no.nav.freg.abac.spring.consumer.AbacRequestMapper;
import no.nav.freg.abac.spring.consumer.AbacResponseMapper;
import no.nav.freg.abac.spring.consumer.AbacRestTemplateConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@Import(AbacConfig.class)
public class MainAbacConfig {
	@Bean
	AbacConsumer abacConsumer(RestTemplate restTemplate, @Value("${abac.url}") String abacUrl) {
		return new AbacRestTemplateConsumer(restTemplate, abacUrl, new AbacRequestMapper(), new AbacResponseMapper());
	}
}
