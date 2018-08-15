package no.nav.dokarkiv.behandlejournal.v3.tjoark061;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.LagreVedleggPaaJournalpostResponse;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link LagreVedleggPaaJournalpostResponseMapper}
 * 
 * @author Rune Romundstad, Visma Consulting
 *
 */
@Component
public class DefaultLagreVedleggPaaJournalpostResponseMapper implements LagreVedleggPaaJournalpostResponseMapper {

	/**{@inheritDoc} */
	@Override
	public LagreVedleggPaaJournalpostResponse map(
			no.nav.dokarkiv.behandlejournal.v3.tjoark061.LagreVedleggPaaJournalpostResponse domainResponse) {
		return new LagreVedleggPaaJournalpostResponse().withDokumentId(domainResponse.getDokumentId().toString());
	}
}
