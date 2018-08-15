package no.nav.dokarkiv.behandlejournal.v3.tjoark060;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.ArkiverUstrukturertKravResponse;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link ArkiverUstrukturertKravResponseMapper}. Maps response from domain to FIM.
 *
 * @author Rune Romundstad, Visma Consulting
 */
@Component
public class DefaultArkiverUstrukturertKravResponseMapper implements ArkiverUstrukturertKravResponseMapper {

	@Override
	public ArkiverUstrukturertKravResponse map(
			no.nav.dokarkiv.behandlejournal.v3.tjoark060.ArkiverUstrukturertKravResponse domainResponse) {
		return new ArkiverUstrukturertKravResponse()
				.withJournalpostId(domainResponse.getJournalpostId().toString())
				.withDokumentId(domainResponse.getDokumentId().toString());
	}
}
