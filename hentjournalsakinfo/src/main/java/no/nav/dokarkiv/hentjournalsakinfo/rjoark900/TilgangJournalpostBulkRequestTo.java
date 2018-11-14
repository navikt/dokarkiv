package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@NoArgsConstructor
public class TilgangJournalpostBulkRequestTo {
	private List<String> gsakSakIds;
	private List<String> psakSakIds;
	private String fraDato;
	private List<FagomradeCode> inkluderTema;
	private List<JournalStatusCode> inkluderJournalStatus;
	private List<JournalpostTypeCode> inkluderJournalpostType;
	private boolean visFeilregistrerte;
	private List<String> alleIdenter;

	public boolean isInkluderMidlertidigeJournalposter() {
		return inkluderJournalStatus.contains(MO) || inkluderJournalStatus.contains(M);
	}
}
