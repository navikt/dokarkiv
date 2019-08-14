package no.nav.dokarkiv.hentjournalsakinfo.rjoark904;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@NoArgsConstructor
public class FinnJournalposterStatusRequestTo {
	private String fraDato;
	private JournalStatusCode journalstatus;
	private List<JournalpostTypeCode> inkluderJournalpostType;
	private Integer foerste;
	private String etterPeker;
}
