package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HentTilgangJournalpostResponse {
	TilgangJournalpostDto tilgangJournalpostDto;

	public HentTilgangJournalpostResponse(@JsonProperty("tilgangJournalpostDto") TilgangJournalpostDto tilgangJournalpostDto) {
		this.tilgangJournalpostDto = tilgangJournalpostDto;
	}
}
