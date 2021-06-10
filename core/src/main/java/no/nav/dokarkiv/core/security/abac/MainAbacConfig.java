package no.nav.dokarkiv.core.security.abac;

import no.nav.dokarkiv.core.logging.AbacLogger;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.freg.abac.core.annotation.context.AbacContext;
import no.nav.freg.abac.core.consumer.AbacConsumer;
import no.nav.freg.abac.core.service.AbacService;
import no.nav.freg.abac.core.service.AbacServiceImpl;
import no.nav.freg.abac.core.service.advice.AdviceStrategy;
import no.nav.freg.abac.core.service.obligation.ObligationStrategy;
import no.nav.freg.abac.spring.config.AbacAnnotationConfig;
import no.nav.freg.abac.spring.consumer.AbacRequestMapper;
import no.nav.freg.abac.spring.consumer.AbacResponseMapper;
import no.nav.freg.abac.spring.consumer.AbacRestTemplateConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@Import(AbacAnnotationConfig.class)
public class MainAbacConfig {

	/*
	 * Hentet fra no.nav.freg.abac.spring.config for å få to abacConsumers
	 */
	@Autowired(required = false)
	private List<ObligationStrategy> obligationStrategies = new ArrayList();
	@Autowired(required = false)
	private List<AdviceStrategy> adviceStrategies = new ArrayList();

	/*
	 * end
	 */

	/*
	 * Hva er beste måte å løse dette på? Disse er for bruk i AbacSecurityService.
	 * Vil det være bedre å gjøre disse ikke final i AbacSecurityService og kjøre field injection der slik at
	 * det eneste jeg trenger å sende med er AbacServicen for opprettelse?
	 * Evt andre tanker?
	 */
	@Inject
	private AbacLogger abaclog = null;
	@Inject
	private AbacContext abacContext;
	@Inject
	private JdbcAbacSecurityRepository jdbcAbacSecurityRepository;
	@Inject
	private DokumentinfoRepository dokumentinfoRepository;
	@Inject
	private JoarkRepositorySkjermet joarkRepositorySkjermet;

	@Bean
	@Primary
	AbacConsumer abacConsumer(RestTemplate restTemplate, @Value("${abac.url}") String abacUrl) {
		return new AbacRestTemplateConsumer(restTemplate, abacUrl, new AbacRequestMapper(), new AbacResponseMapper());
	}

	@Bean
	@Primary
	AbacService abacArkivService(AbacConsumer abacConsumer) {
		return new AbacServiceImpl(this.obligationStrategies, this.adviceStrategies, abacConsumer);
	}

	@Bean
	AbacConsumer abacArkivV2Consumer(RestTemplate restTemplate, @Value("${abac.arkiv.v2.url}") String abacUrl) {
		return new AbacRestTemplateConsumer(restTemplate, abacUrl, new AbacRequestMapper(), new AbacResponseMapper());
	}

	@Bean
	AbacService abacArkivV2Service(@Qualifier("abacArkivV2Consumer") AbacConsumer abacArkivV2Consumer) {
		return new AbacServiceImpl(this.obligationStrategies, this.adviceStrategies, abacArkivV2Consumer);
	}

	@Bean
	AbacSecurityService abacArkivV2SecurityService(@Qualifier("abacArkivV2Service") AbacService abacArkivV2Service) {
		return new AbacSecurityService(abaclog, abacArkivV2Service,
				abacContext, jdbcAbacSecurityRepository,
				dokumentinfoRepository, joarkRepositorySkjermet);
	}

}
