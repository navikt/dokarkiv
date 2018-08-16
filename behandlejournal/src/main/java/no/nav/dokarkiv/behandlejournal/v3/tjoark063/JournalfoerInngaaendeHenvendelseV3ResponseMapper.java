package no.nav.dokarkiv.behandlejournal.v3.tjoark063;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerInngaaendeHenvendelseResponse;
import org.springframework.stereotype.Component;

@Component
public class JournalfoerInngaaendeHenvendelseV3ResponseMapper {

	public JournalfoerInngaaendeHenvendelseResponse map(
			no.nav.dokarkiv.behandlejournal.v3.tjoark063.JournalfoerInngaaendeHenvendelseResponse domainResponse) {
		return new JournalfoerInngaaendeHenvendelseResponse().withJournalpostId(domainResponse.getJournalpostId().toString());
	}
}
