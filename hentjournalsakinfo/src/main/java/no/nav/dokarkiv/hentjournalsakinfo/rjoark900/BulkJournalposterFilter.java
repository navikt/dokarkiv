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
class BulkJournalposterFilter {
	public static final long JOURNALPOST_ID_MAX = 999999999L;
	private final LocalDate fraDato;
	private final List<String> alleIdenter;
	private final List<String> inkluderTema;
	private final List<String> inkluderJournalStatus;
	private final List<String> inkluderJournalpostType;
	private final boolean visFeilregistrerte;
	private final int antallRader;
	private final Long journalpostIdPeker;

	BulkJournalposterFilter(HentJournalpostBulkRequestTo hentJournalpostBulkRequestTo) {
		this.fraDato = LocalDate.parse(hentJournalpostBulkRequestTo.getFraDato());
		this.alleIdenter = hentJournalpostBulkRequestTo.getAlleIdenter();
		this.inkluderTema = hentJournalpostBulkRequestTo.getInkluderTema().stream().map(Enum::name).collect(Collectors.toList());
		this.inkluderJournalStatus = hentJournalpostBulkRequestTo.getInkluderJournalStatus().stream().map(Enum::name).collect(Collectors.toList());
		this.inkluderJournalpostType = hentJournalpostBulkRequestTo.getInkluderJournalpostType().stream().map(Enum::name).collect(Collectors.toList());
		this.visFeilregistrerte = hentJournalpostBulkRequestTo.isVisFeilregistrerte();
		this.antallRader = hentJournalpostBulkRequestTo.getFoerste();
		this.journalpostIdPeker = hentPeker(hentJournalpostBulkRequestTo);
	}

	private Long hentPeker(HentJournalpostBulkRequestTo hentJournalpostBulkRequestTo) {
		if (hentJournalpostBulkRequestTo.getPeker() == null) {
			return JOURNALPOST_ID_MAX;
		} else {
			try {
				return Long.parseLong(new String(Base64.getDecoder().decode(hentJournalpostBulkRequestTo.getPeker())));
			} catch (IllegalArgumentException e) {
				return JOURNALPOST_ID_MAX;
			}
		}
	}

	boolean isKunFeilregistrerte() {
		return inkluderJournalStatus.isEmpty() && visFeilregistrerte;
	}

	boolean isInkluderMidlertidigeJournalposter() {
		return inkluderJournalStatus.contains(MO.name()) || inkluderJournalStatus.contains(M.name());
	}
}
