package no.nav.dokarkiv.hentjournalsakinfo;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO;
import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.Value;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.FinnJournalposterRequestTo;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark904.FinnJournalposterStatusRequestTo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class JournalpostFilter {
	public static final long JOURNALPOST_ID_MAX = 999999999L;
	public static final long JOURNALPOST_ID_MIN = 0L;
	private final LocalDate fraDato;
	private final LocalDate tilDato;
	private final List<String> alleIdenter;
	private final List<String> inkluderJournalStatus;
	private final List<String> inkluderJournalpostType;
	private final boolean visFeilregistrerte;
	private final int antallRader;
	private final Long journalpostIdPeker;

	public JournalpostFilter(FinnJournalposterRequestTo finnJournalposterRequestTo) {
		this.fraDato = LocalDate.parse(finnJournalposterRequestTo.getFraDato());
		if(isBlank(finnJournalposterRequestTo.getTilDato())) {
			this.tilDato = null;
		} else {
			this.tilDato = LocalDate.parse(finnJournalposterRequestTo.getTilDato());
		}
		this.alleIdenter = finnJournalposterRequestTo.getAlleIdenter();
		this.inkluderJournalStatus = finnJournalposterRequestTo.getInkluderJournalStatus().stream().map(Enum::name).collect(Collectors.toList());
		this.inkluderJournalpostType = finnJournalposterRequestTo.getInkluderJournalpostType().stream().map(Enum::name).collect(Collectors.toList());
		this.visFeilregistrerte = finnJournalposterRequestTo.isVisFeilregistrerte();
		this.antallRader = finnJournalposterRequestTo.getFoerste();
		this.journalpostIdPeker = getPeker(finnJournalposterRequestTo.getEtterPeker());
	}

	public JournalpostFilter(FinnJournalposterStatusRequestTo finnJournalposterStatusRequestTo) {
		this.fraDato = LocalDate.parse(finnJournalposterStatusRequestTo.getFraDato());
		// Ikke brukt i denne
		this.tilDato = null;
		this.inkluderJournalStatus = Collections.singletonList(finnJournalposterStatusRequestTo.getJournalstatus().toString());
		this.inkluderJournalpostType = finnJournalposterStatusRequestTo.getJournalposttyper().stream().map(Enum::name).collect(Collectors.toList());
		// Kun tillatt å paginere forover
		this.antallRader = finnJournalposterStatusRequestTo.getFoerste();
		this.journalpostIdPeker = getPeker(finnJournalposterStatusRequestTo.getEtterPeker());
		// Ikke brukt
		this.alleIdenter = new ArrayList<>();
		this.visFeilregistrerte = false;
	}

	private Long getPeker(String peker) {
		if (peker == null) {
			return JournalpostFilter.JOURNALPOST_ID_MAX;
		}
		try {
			return Long.parseLong(new String(Base64.getDecoder().decode(peker)));
		} catch (IllegalArgumentException e) {
			return JournalpostFilter.JOURNALPOST_ID_MAX;
		}
	}

	public boolean isKunFeilregistrerte() {
		return inkluderJournalStatus.isEmpty() && visFeilregistrerte;
	}

	public boolean isInkluderMidlertidigeJournalposter() {
		return alleIdenter != null && !alleIdenter.isEmpty() &&
				(inkluderJournalStatus.contains(MO.name()) ||
						inkluderJournalStatus.contains(M.name()) ||
						inkluderJournalStatus.contains(D.name()));
	}
}
