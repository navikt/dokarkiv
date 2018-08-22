package no.nav.dokarkiv.behandlejournal.v3.tjoark061;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.LagreVedleggPaaJournalpostResponse;
import org.springframework.stereotype.Component;

@Component
public class LagreVedleggPaaJournalpostV3ResponseMapper  {

	public LagreVedleggPaaJournalpostResponse map(
			no.nav.dokarkiv.behandlejournal.v3.tjoark061.LagreVedleggPaaJournalpostResponse domainResponse) {
		return new LagreVedleggPaaJournalpostResponse().withDokumentId(domainResponse.getDokumentId().toString());
	}
}
