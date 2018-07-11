package no.nav.dokarkiv.behandlejournal.v2.tjoark064;

import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerUtgaaendeHenvendelseResponse;
import org.springframework.stereotype.Component;

/**
 * Implementation of
 * JournalfoerUtgaaendeHenvendelseResponseMapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 *
 */
@Component
public class DefaultJournalfoerUtgaaendeHenvendelseResponseMapper implements
		JournalfoerUtgaaendeHenvendelseResponseMapper {

	@Override
	public JournalfoerUtgaaendeHenvendelseResponse map(
			no.nav.dokarkiv.behandlejournal.v2.tjoark064.JournalfoerUtgaaendeHenvendelseResponse domainResponse) {
		return new JournalfoerUtgaaendeHenvendelseResponse().withJournalpostId(domainResponse.getJournalpostId().toString());
	}
}
