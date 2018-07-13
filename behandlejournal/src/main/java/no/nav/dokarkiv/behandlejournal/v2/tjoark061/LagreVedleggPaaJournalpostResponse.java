package no.nav.dokarkiv.behandlejournal.v2.tjoark061;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Domain response object for service LagreVedleggPaaJournalpost.
 * 
 * @author Rune Romundstad, Visma Consulting
 *
 */
public class LagreVedleggPaaJournalpostResponse {

	private Long dokumentId;
	
	/** Default Constructor needed for mapping. */
	@SuppressWarnings("unused")
	private LagreVedleggPaaJournalpostResponse(){	
	}
	
	/**
	 * Constructor to create a response.
	 * @param dokumentId the dokumentId in response.
	 */
	public LagreVedleggPaaJournalpostResponse(Long dokumentId) {
		this.dokumentId = dokumentId;
	}

	/**
	 * Getter for the dokumentId property.
	 * 
	 * @return the dokumentId
	 */
	public Long getDokumentId() {
		return dokumentId;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("dokumentId", dokumentId)
			.toString();
	}

}
