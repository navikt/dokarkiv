package no.nav.dokarkiv.innsynjournal.v2.tjoark059;

import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;

/**
 * Transport object for {@link IdentifiserJournalpostService }
 *
 * @author Ketill Fenne, Visma Consulting
 */
public class IdentifiserJournalpostToRequest {

	private String kanalReferanseId;
	private MottaksKanalCode mottaksKanal;

	public IdentifiserJournalpostToRequest() {}

	public String getKanalReferanseId() {
		return kanalReferanseId;
	}

	public MottaksKanalCode getMottaksKanal() {
		return mottaksKanal;
	}

	public void setKanalReferanseId(String kanalReferanseId) {
		this.kanalReferanseId = kanalReferanseId;
	}

	public void setMottaksKanal(MottaksKanalCode mottaksKanal) {
		this.mottaksKanal = mottaksKanal;
	}
}
