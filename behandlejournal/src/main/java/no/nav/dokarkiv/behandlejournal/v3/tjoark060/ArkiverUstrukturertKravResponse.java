package no.nav.dokarkiv.behandlejournal.v3.tjoark060;

/**
 * Response object for the ArkiverUstrukturertKrav service.
 * 
 * @author Rune Romundstad, Visma Consulting
 *
 */
public class ArkiverUstrukturertKravResponse {

	private final Long journalpostId;
	private final Long dokumentId;

	/**
	 * Constructs a new ArkiverUstrukturertKravResponse.
	 *
	 * @param journalpostId The journalpostId
	 * @param dokumentId The dokumentId
	 */
	public ArkiverUstrukturertKravResponse(Long journalpostId, Long dokumentId) {
		this.journalpostId = journalpostId;
		this.dokumentId = dokumentId;
	}

	/**
	 * Getter for the journalpostId property.
	 *
	 * @return the journalpostId
	 */
	public Long getJournalpostId() {
		return journalpostId;
	}
	
	/**
	 * Getter for the dokumentId property.
	 *
	 * @return the dokumentId
	 */
	public Long getDokumentId() {
		return dokumentId;
	}

}
