package no.nav.dokarkiv.behandlejournal.v3.tjoark060;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.ArkiverUstrukturertKravResponse;
import org.springframework.stereotype.Component;

@Component
public class ArkiverUstrukturertKravV3ResponseMapper {

	public ArkiverUstrukturertKravResponse map(
			no.nav.dokarkiv.behandlejournal.v3.tjoark060.ArkiverUstrukturertKravResponse domainResponse) {
		return new ArkiverUstrukturertKravResponse()
				.withJournalpostId(domainResponse.getJournalpostId().toString())
				.withDokumentId(domainResponse.getDokumentId().toString());
	}
}
