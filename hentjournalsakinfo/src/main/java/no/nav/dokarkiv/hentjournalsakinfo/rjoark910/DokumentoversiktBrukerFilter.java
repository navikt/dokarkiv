package no.nav.dokarkiv.hentjournalsakinfo.rjoark910;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO;

import lombok.Value;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class DokumentoversiktBrukerFilter {
	public static final long JOURNALPOST_ID_MAX = 999999999L;
	public static final long JOURNALPOST_ID_MIN = 0L;
	private final String aktoerId;
	private final String orgnr;
	private final List<String> psakIds;
	private final LocalDate fraDato;
	private final List<String> alleIdenter;
	private final List<String> inkluderJournalStatus;
	private final List<String> inkluderJournalpostType;
	private final boolean visFeilregistrerte;
	private final int antallRader;
	private final Long journalpostIdPeker;

	DokumentoversiktBrukerFilter(DokumentoversiktBrukerRequestTo dokumentoversiktBrukerRequestTo) {
		this.aktoerId = dokumentoversiktBrukerRequestTo.getAktoerId();
		this.orgnr = dokumentoversiktBrukerRequestTo.getOrgnr();
		if(dokumentoversiktBrukerRequestTo.getPsakSakIds() == null) {
			this.psakIds = new ArrayList<>();
		} else {
			this.psakIds = new ArrayList<>(dokumentoversiktBrukerRequestTo.getPsakSakIds());
		}
		this.fraDato = dokumentoversiktBrukerRequestTo.getFraDato();
		this.alleIdenter = dokumentoversiktBrukerRequestTo.getAlleIdenter();
		this.inkluderJournalStatus = dokumentoversiktBrukerRequestTo.getInkluderJournalStatus().stream().map(Enum::name).collect(Collectors.toList());
		this.inkluderJournalpostType = dokumentoversiktBrukerRequestTo.getInkluderJournalpostType().stream().map(Enum::name).collect(Collectors.toList());
		this.visFeilregistrerte = dokumentoversiktBrukerRequestTo.isVisFeilregistrerte();
		this.antallRader = dokumentoversiktBrukerRequestTo.getFoerste();
		this.journalpostIdPeker = getPeker(dokumentoversiktBrukerRequestTo.getEtter(), JOURNALPOST_ID_MAX);
	}

	private Long getPeker(String peker, Long defaultValue) {
		if (peker == null) {
			return defaultValue;
		}
		try {
			return Long.parseLong(new String(Base64.getDecoder().decode(peker)));
		} catch (IllegalArgumentException e) {
			return defaultValue;
		}
	}

	boolean isBrukerPerson() {
		return aktoerId != null;
	}

	boolean isBrukerOrganisasjon() {
		return aktoerId == null && orgnr != null;
	}

	boolean containsPsakSaker() {
		return !psakIds.isEmpty();
	}

	boolean isKunFeilregistrerte() {
		return inkluderJournalStatus.isEmpty() && visFeilregistrerte;
	}

	boolean isInkluderMidlertidigeJournalposter() {
		return alleIdenter != null && !alleIdenter.isEmpty() &&
				(inkluderJournalStatus.contains(MO.name()) ||
						inkluderJournalStatus.contains(M.name()) ||
						inkluderJournalStatus.contains(D.name()));
	}
}
