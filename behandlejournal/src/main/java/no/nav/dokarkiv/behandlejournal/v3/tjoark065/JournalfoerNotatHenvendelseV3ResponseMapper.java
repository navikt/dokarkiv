package no.nav.dokarkiv.behandlejournal.v3.tjoark065;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerNotatResponse;
import org.springframework.stereotype.Component;

@Component
public class JournalfoerNotatHenvendelseV3ResponseMapper {

	public JournalfoerNotatResponse map(
			JournalfoerNotatHenvendelseResponse domainResponse) {
		return new JournalfoerNotatResponse().withJournalpostId(domainResponse.getJournalpostId().toString());
	}
}
