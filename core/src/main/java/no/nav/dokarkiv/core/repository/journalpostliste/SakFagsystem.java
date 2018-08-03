package no.nav.dokarkiv.core.repository.journalpostliste;

import no.nav.dokarkiv.core.domain.codes.FagsystemCode;

/**
 * Common object used by TJOARK-053 and TJOARK-055.
 * 
 * Created by Hans Petter Simonsen - Visma Consulting.
 */
public class SakFagsystem {
    private String sakId;
    private FagsystemCode fagsystem;

    public SakFagsystem() {
    }

    public SakFagsystem(FagsystemCode fagsystem, String sakId) {
        this.fagsystem = fagsystem;
        this.sakId = sakId;
    }

    public FagsystemCode getFagsystem() {
        return fagsystem;
    }

    public void setFagsystem(FagsystemCode fagsystem) {
        this.fagsystem = fagsystem;
    }

    public String getSakId() {
        return sakId;
    }

    public void setSakId(String sakId) {
        this.sakId = sakId;
    }

	@Override
	public String toString() {
		return "SakFagsystem [sakId=" + sakId + ", fagsystem=" + fagsystem + "]";
	}
}
