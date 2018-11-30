package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO;

import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
class BulkJournalposterFilter {
	private final LocalDate fraDato;
	private final List<String> alleIdenter;
	private final List<String> inkluderTema;
	private final List<String> inkluderJournalStatus;
	private final List<String> inkluderJournalpostType;
	private final boolean visFeilregistrerte;

	BulkJournalposterFilter(String fraDato,
							List<String> alleIdenter,
							List<FagomradeCode> inkluderTema,
							List<JournalStatusCode> inkluderJournalStatus,
							List<JournalpostTypeCode> inkluderJournalpostType,
							boolean visFeilregistrerte) {
		this.fraDato = LocalDate.parse(fraDato);
		this.alleIdenter = alleIdenter;
		this.inkluderTema = inkluderTema.stream().map(Enum::name).collect(Collectors.toList());
		this.inkluderJournalStatus = inkluderJournalStatus.stream().map(Enum::name).collect(Collectors.toList());
		this.inkluderJournalpostType = inkluderJournalpostType.stream().map(Enum::name).collect(Collectors.toList());
		this.visFeilregistrerte = visFeilregistrerte;
	}

	boolean isKunFeilregistrerte() {
		return inkluderJournalStatus.isEmpty() && visFeilregistrerte;
	}

	boolean isInkluderMidlertidigeJournalposter() {
		return inkluderJournalStatus.contains(MO.name()) || inkluderJournalStatus.contains(M.name());
	}
}
