package no.nav.dokarkiv.innsynjournal.v2.tjoark053;

import java.util.ArrayList;
import java.util.List;

/**
 * Transport object for {@link HentMinTilgjengeligeJournalpostListeService }
 *
 * @author Torgeir Cook, Visma Consulting
 */
public class HentJournalpostListeToRequest {

	private boolean merkInnsynDokument;
	private List<SakFagsystem> saksListe;

	public HentJournalpostListeToRequest() {
		this.saksListe = new ArrayList<>();
	}

	public boolean isMerkInnsynDokument() {
		return merkInnsynDokument;
	}

	public void setMerkInnsynDokument(boolean merkInnsynDokument) {
		this.merkInnsynDokument = merkInnsynDokument;
	}

	public List<SakFagsystem> getSaksListe() {
		return saksListe;
	}
}
