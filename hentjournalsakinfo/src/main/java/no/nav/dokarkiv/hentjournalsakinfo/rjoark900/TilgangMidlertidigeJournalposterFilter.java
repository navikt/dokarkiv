package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class TilgangMidlertidigeJournalposterFilter {
	private static final List<JournalStatusCode> MIDLERTIDIGE_JOURNALPOSTER = Arrays.asList(JournalStatusCode.M, JournalStatusCode.MO);
	private final LocalDate fraDato;
	private final List<FagomradeCode> inkluderTema;
	private final List<JournalStatusCode> inkluderJournalStatus;
	private final List<JournalpostTypeCode> inkluderJournalpostType;

	public TilgangMidlertidigeJournalposterFilter(String fraDato,
												  List<FagomradeCode> inkluderTema,
												  List<JournalpostTypeCode> inkluderJournalpostType) {
		this.fraDato = LocalDate.parse(fraDato);
		this.inkluderTema = inkluderTema;
		this.inkluderJournalStatus = MIDLERTIDIGE_JOURNALPOSTER;
		this.inkluderJournalpostType = inkluderJournalpostType;
	}
}
