package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

/**
 * The response object for the OpprettOgFerdigstillJournalpost service.
 *
 * @author Torgeir Cook.
 */
public class OpprettJournalpostArkiverDokumentResponseTo {

	private Long journalpostId;

	private Long dokumentInfoId;

	/**
	 * Constructor with parameters
	 *
	 * @param journalpostId  The journalpostId
	 * @param dokumentInfoId dokumentInfoId
	 */
	public OpprettJournalpostArkiverDokumentResponseTo(Long journalpostId, Long dokumentInfoId) {
		this.journalpostId = journalpostId;
		this.dokumentInfoId = dokumentInfoId;
	}

	public Long getJournalpostId() {
		return journalpostId;
	}

	public Long getDokumentInfoId() {
		return dokumentInfoId;
	}
}
