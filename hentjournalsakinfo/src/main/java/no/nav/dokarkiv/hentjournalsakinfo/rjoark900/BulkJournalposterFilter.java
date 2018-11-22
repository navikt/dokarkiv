package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class BulkJournalposterFilter {
	private final LocalDate fraDato;
	private final List<JournalStatusCode> inkluderJournalStatus;
	private final List<JournalpostTypeCode> inkluderJournalpostType;
	private final boolean visFeilregistrerte;

	public BulkJournalposterFilter(String fraDato,
								   List<JournalStatusCode> inkluderJournalStatus,
								   List<JournalpostTypeCode> inkluderJournalpostType,
								   boolean visFeilregistrerte) {
		this.fraDato = LocalDate.parse(fraDato);
		this.inkluderJournalStatus = inkluderJournalStatus;
		this.inkluderJournalpostType = inkluderJournalpostType;
		this.visFeilregistrerte = visFeilregistrerte;
	}
}
