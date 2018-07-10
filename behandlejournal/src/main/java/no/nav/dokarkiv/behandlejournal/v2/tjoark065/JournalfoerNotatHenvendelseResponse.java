package no.nav.dokarkiv.behandlejournal.v2.tjoark065;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The response object for the JournalfoerNotatHenvendelse service.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class JournalfoerNotatHenvendelseResponse {

	private Long journalpostId;

	/**
	 * Needed for mapping
	 */
	@SuppressWarnings("unused")
	private JournalfoerNotatHenvendelseResponse() {
	}

	/**
	 * Constructor with parameters
	 *
	 * @param journalpostId The journalpostId
	 */
	public JournalfoerNotatHenvendelseResponse(Long journalpostId) {
		this.journalpostId = journalpostId;
	}

	/**
	 * Gets the value of the journalpostId property.
	 *
	 * @return the journalpostId
	 */
	public Long getJournalpostId() {
		return journalpostId;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("journalpostId", journalpostId).toString();
	}
}
