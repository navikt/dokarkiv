package no.nav.dokarkiv.core.domain;

import no.nav.repository.dok.joark.mod.JoarkRepository;
import no.nav.service.dok.joark.mod.ping.PingService;

import javax.inject.Inject;

/**
 * Implementation of PingService.
 *
 * @author Rune Romundstad, Visma Consulting
 */
public class DefaultPingService implements PingService {

	@Inject
	private JoarkRepository joarkRepository;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void ping() {
		joarkRepository.countJournalpostTyper();
	}

}
