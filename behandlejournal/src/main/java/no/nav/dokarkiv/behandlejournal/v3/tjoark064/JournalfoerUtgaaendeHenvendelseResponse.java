package no.nav.dokarkiv.behandlejournal.v3.tjoark064;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The response object for the JournalfoerUtgaaendeHenvendelse
 * service.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class JournalfoerUtgaaendeHenvendelseResponse {

	private Long journalpostId;

	/**
	 * Needed for mapping
	 */
	@SuppressWarnings("unused")
	private JournalfoerUtgaaendeHenvendelseResponse() {
	}

	/**
	 * Constructor with parameters
	 *
	 * @param journalpostId The journalpostId
	 */
	public JournalfoerUtgaaendeHenvendelseResponse(Long journalpostId) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("journalpostId", journalpostId).toString();
	}
}
