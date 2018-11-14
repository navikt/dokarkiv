package no.nav.dokarkiv.hentjournalsakinfo.rjoark910;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@NoArgsConstructor
public class VisningJournalpostBulkRequestTo {
	private List<String> journalpostIds;
}
