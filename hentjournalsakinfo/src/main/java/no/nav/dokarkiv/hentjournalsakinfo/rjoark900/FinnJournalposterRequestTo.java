package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

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
public class FinnJournalposterRequestTo {
	private List<String> gsakSakIds;
	private List<String> psakSakIds;
	private String fraDato;
	private List<JournalStatusCode> inkluderJournalStatus;
	private List<JournalpostTypeCode> inkluderJournalpostType;
	private boolean visFeilregistrerte;
	private List<String> alleIdenter;
	private Integer foerste;
	private String etterPeker;
	private Integer siste;
	private String foerPeker;
}
