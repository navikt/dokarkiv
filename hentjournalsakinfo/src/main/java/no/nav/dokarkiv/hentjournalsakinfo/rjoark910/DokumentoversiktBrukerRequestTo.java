package no.nav.dokarkiv.hentjournalsakinfo.rjoark910;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@NoArgsConstructor
public class DokumentoversiktBrukerRequestTo {
	private String aktoerId;
	private String orgnr;
	private List<String> psakSakIds;
	private LocalDate fraDato;
	private List<JournalStatusCode> inkluderJournalStatus;
	private List<JournalpostTypeCode> inkluderJournalpostType;
	private boolean visFeilregistrerte;
	@ToString.Exclude
	private List<String> alleIdenter;
	private Integer foerste;
	private String etter;
}
