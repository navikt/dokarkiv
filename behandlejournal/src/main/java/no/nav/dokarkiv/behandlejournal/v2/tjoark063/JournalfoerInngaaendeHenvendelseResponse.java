package no.nav.dokarkiv.behandlejournal.v2.tjoark063;


/**
 * Domain response object for the JournalfoerInngaaendeHenvendelse service.
 *
 * @author Rune Romundstad, Visma Consulting
 */
public class JournalfoerInngaaendeHenvendelseResponse {

	private Long journalpostId;

	/**
	 * Default constructor used for mapping only.
	 */
	@SuppressWarnings("unused")
	private JournalfoerInngaaendeHenvendelseResponse() {
	}

	/**
	 * Constructor taking fields of response as parameters.
	 *
	 * @param journalpostId The id of the created Journalpost
	 */
	public JournalfoerInngaaendeHenvendelseResponse(Long journalpostId) {
		this.journalpostId = journalpostId;
	}

	/**
	 * Getter for the journalpostId property.
	 *
	 * @return the journalpostId
	 */
	public Long getJournalpostId() {
		return journalpostId;
	}

}
