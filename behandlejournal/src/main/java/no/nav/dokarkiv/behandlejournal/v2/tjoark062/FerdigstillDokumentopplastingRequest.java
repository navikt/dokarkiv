package no.nav.dokarkiv.behandlejournal.v2.tjoark062;

import lombok.Builder;
import no.nav.dokarkiv.behandlejournal.SporingsMetaData;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Request object for the FerdigstillDokumentOpplasting operation.
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 *
 */
@Builder
public class FerdigstillDokumentopplastingRequest {
	
	private Long journalpostId;
	private SporingsMetaData sporingsMetaData;
	
	/**
	 * Used for mapping
	 */
	@SuppressWarnings("unused")
	private FerdigstillDokumentopplastingRequest() {
	}
	
	/**
	 * Constructor that sets the journalpostId
	 * 
	 * @param journalpostId
	 */
	public FerdigstillDokumentopplastingRequest(Long journalpostId, SporingsMetaData sporingsMetaData) {
		this.journalpostId = journalpostId;
		this.sporingsMetaData = sporingsMetaData;
	}
	
	/**
	 * Gets the journalpostId
	 * 
	 * @return The journalpostId
	 */
	public Long getJournalpostId() {
		return journalpostId;
	}

	/**
	 * Getter for the sporingsMetaData property.
	 * 
	 * @return the sporingsMetaData
	 */
	public SporingsMetaData getSporingsMetaData() {
		return sporingsMetaData;
	}
	
	/**
	 * Check that journalpostId is set. If not, throw {@link ApplicationException}.
	 */
	public void validate() {
		if (journalpostId == null) {
			throw new ApplicationException("Missing parameter: journalpostId");
		}
		if (sporingsMetaData == null) {
			throw new ApplicationException("Missing parameter: sporingsMetaData");
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("journalpostId", journalpostId).toString();
	}
}
