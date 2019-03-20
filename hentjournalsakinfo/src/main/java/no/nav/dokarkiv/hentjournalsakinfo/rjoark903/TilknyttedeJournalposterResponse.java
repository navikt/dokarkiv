package no.nav.dokarkiv.hentjournalsakinfo.rjoark903;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class TilknyttedeJournalposterResponse {
	private final List<TilknyttetJournalpostDto> tilknyttedeJournalposter;

	public TilknyttedeJournalposterResponse(@JsonProperty("tilknyttedeJournalposter") List<TilknyttetJournalpostDto> tilknyttedeJournalposter) {
		this.tilknyttedeJournalposter = tilknyttedeJournalposter;
	}
}
