package no.nav.dokarkiv.hentjournalsakinfo.rjoark910;

import lombok.Builder;
import lombok.Data;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class DokumentInfoDto {
	private Long dokumentInfoId;
	private String tittel;
	private Long originalJournalpostId;
}
