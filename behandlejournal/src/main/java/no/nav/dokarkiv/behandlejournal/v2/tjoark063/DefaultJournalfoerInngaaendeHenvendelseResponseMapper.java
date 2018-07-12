package no.nav.dokarkiv.behandlejournal.v2.tjoark063;

import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerInngaaendeHenvendelseResponse;
import org.springframework.stereotype.Component;

/**
 * Implementation of
 * JournalfoerInngaaendeHenvendelseResponseMapper
 *
 * @author Rune Romundstad, Visma Consulting
 */
@Component
public class DefaultJournalfoerInngaaendeHenvendelseResponseMapper implements
		JournalfoerInngaaendeHenvendelseResponseMapper {

	@Override
	public JournalfoerInngaaendeHenvendelseResponse map(
			no.nav.dokarkiv.behandlejournal.v2.tjoark063.JournalfoerInngaaendeHenvendelseResponse domainResponse) {
		return new JournalfoerInngaaendeHenvendelseResponse().withJournalpostId(domainResponse.getJournalpostId().toString());
	}
}
