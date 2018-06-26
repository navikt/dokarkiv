package no.nav.dokarkiv.core.domain;

import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.dozer.inject.Inject;
import org.springframework.stereotype.Component;

/**
 * Implementation of PingService.
 *
 * @author Rune Romundstad, Visma Consulting
 */
@Component
public class DefaultPingService {

	@Inject
	private JoarkRepository joarkRepository;

	/**
	 * {@inheritDoc}
	 */
	public void ping() {
		joarkRepository.count();
	}

}
