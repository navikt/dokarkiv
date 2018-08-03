package no.nav.dokarkiv.journal.v3.tjoark058;

import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.repository.journalpostliste.SakFagsystem;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class HentKjerneJournalpostListeRequestTo {
	private List<SakFagsystem> saksListe;
	private JournalpostTypeCode journalpostType;
	private List<FagomradeCode> tema;
	private Date journalFom;
	private Date journalTom;
	private long resultatSettStoerrelse;
	private int resultatSettNr;		
}
