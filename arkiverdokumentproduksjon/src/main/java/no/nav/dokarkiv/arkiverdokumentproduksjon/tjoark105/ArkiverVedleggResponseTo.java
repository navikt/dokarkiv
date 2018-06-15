package no.nav.service.dok.joark.nsb.to;

/**
 * @author Magnar Brandsdal, Visma Consulting
 */
public class ArkiverVedleggResponseTo {

	private long journalpostId;
	private long dokumentInfoId;

	private ArkiverVedleggResponseTo() {}

	public static ArkiverVedleggResponseTo create(long journalpostId, long dokumentInfoId) {
		ArkiverVedleggResponseTo to = new ArkiverVedleggResponseTo();
		to.journalpostId = journalpostId;
		to.dokumentInfoId = dokumentInfoId;
		return to;
	}

	public long getJournalpostId() {
		return journalpostId;
	}

	public long getDokumentInfoId() {
		return dokumentInfoId;
	}
}
