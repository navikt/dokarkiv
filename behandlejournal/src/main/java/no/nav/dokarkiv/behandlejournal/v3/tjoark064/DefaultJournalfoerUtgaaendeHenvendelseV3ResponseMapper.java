package no.nav.dokarkiv.behandlejournal.v3.tjoark064;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerUtgaaendeHenvendelseResponse;
import org.springframework.stereotype.Component;

/**
 * Implementation of
 * JournalfoerUtgaaendeHenvendelseResponseMapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 *
 */
@Component
public class DefaultJournalfoerUtgaaendeHenvendelseV3ResponseMapper implements
		JournalfoerUtgaaendeHenvendelseResponseMapper {

	@Override
	public JournalfoerUtgaaendeHenvendelseResponse map(
			no.nav.dokarkiv.behandlejournal.v3.tjoark064.JournalfoerUtgaaendeHenvendelseResponse domainResponse) {
		return new JournalfoerUtgaaendeHenvendelseResponse().withJournalpostId(domainResponse.getJournalpostId().toString());
	}
}
