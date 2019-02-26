package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO;

import lombok.Value;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
class JournalpostFilter {
	public static final long JOURNALPOST_ID_MAX = 999999999L;
	public static final long JOURNALPOST_ID_MIN = 0L;
	private final LocalDate fraDato;
	private final List<String> alleIdenter;
	private final List<String> inkluderJournalStatus;
	private final List<String> inkluderJournalpostType;
	private final boolean visFeilregistrerte;
	private final int antallRader;
	private final Slice slice;
	private final Long journalpostIdPeker;

	enum Slice {
		FOERSTE,
		SISTE
	}

	JournalpostFilter(FinnJournalposterRequestTo finnJournalposterRequestTo) {
		this.fraDato = LocalDate.parse(finnJournalposterRequestTo.getFraDato());
		this.alleIdenter = finnJournalposterRequestTo.getAlleIdenter();
		this.inkluderJournalStatus = finnJournalposterRequestTo.getInkluderJournalStatus().stream().map(Enum::name).collect(Collectors.toList());
		this.inkluderJournalpostType = finnJournalposterRequestTo.getInkluderJournalpostType().stream().map(Enum::name).collect(Collectors.toList());
		this.visFeilregistrerte = finnJournalposterRequestTo.isVisFeilregistrerte();
		this.antallRader = getAntallRader(finnJournalposterRequestTo);
		this.slice = getSlice(finnJournalposterRequestTo);
		this.journalpostIdPeker = getPeker(this.slice, finnJournalposterRequestTo);
	}

	private Integer getAntallRader(FinnJournalposterRequestTo finnJournalposterRequestTo) {
		if(finnJournalposterRequestTo.getFoerste() == null) {
			return finnJournalposterRequestTo.getSiste();
		} else {
			return finnJournalposterRequestTo.getFoerste();
		}
	}

	private Slice getSlice(FinnJournalposterRequestTo finnJournalposterRequestTo) {
		if(finnJournalposterRequestTo.getFoerste() == null) {
			return Slice.SISTE;
		} else {
			return Slice.FOERSTE;
		}
	}

	private Long getPeker(Slice slice, FinnJournalposterRequestTo requestTo) {
		switch(slice) {
			case FOERSTE:
				return getPeker(requestTo.getEtterPeker(), JOURNALPOST_ID_MAX);
			case SISTE:
				return getPeker(requestTo.getFoerPeker(), JOURNALPOST_ID_MIN);
			default:
				return 0L;
		}
	}

	private Long getPeker(String peker, Long defaultValue) {
		if(peker == null) {
			return defaultValue;
		}
		try {
			return Long.parseLong(new String(Base64.getDecoder().decode(peker)));
		} catch (IllegalArgumentException e) {
			return defaultValue;
		}
	}

	boolean isKunFeilregistrerte() {
		return inkluderJournalStatus.isEmpty() && visFeilregistrerte;
	}

	boolean isInkluderMidlertidigeJournalposter() {
		return !alleIdenter.isEmpty() && (inkluderJournalStatus.contains(MO.name()) || inkluderJournalStatus.contains(M.name()));
	}
}
