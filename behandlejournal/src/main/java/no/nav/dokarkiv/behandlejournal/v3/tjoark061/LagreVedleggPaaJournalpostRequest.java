package no.nav.dokarkiv.behandlejournal.v3.tjoark061;

import lombok.Builder;
import no.nav.dokarkiv.behandlejournal.v2.SporingsMetaData;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Domain request object for service LagreVedleggPaaJournalpost.
 * 
 * @author Rune Romundstad, Visma Consulting
 * 
 */
@Builder
public class LagreVedleggPaaJournalpostRequest {

	private Long journalpostId;
	private DokumentInfo dokumentInfo;
	private SporingsMetaData sporingsMetaData;

	@SuppressWarnings("unused")
	private LagreVedleggPaaJournalpostRequest() {

	}

	public LagreVedleggPaaJournalpostRequest(Long journalpostId, DokumentInfo dokumentInfo,
											 SporingsMetaData sporingsMetaData) {
		this.dokumentInfo = dokumentInfo;
		this.journalpostId = journalpostId;
		this.sporingsMetaData = sporingsMetaData;
	}

	/** Validate request. */
	public void validate() {
		if (journalpostId == null) {
			throw new ApplicationException("Missing parameter in request: journalpostId");
		}
		if (dokumentInfo == null) {
			throw new ApplicationException("Missing parameter in request: dokumentInfo");
		}
		if (sporingsMetaData == null) {
			throw new ApplicationException("Missing parameter in request: sporingsMetaData");
		}
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
	 * Getter for the dokumentInfo property.
	 * 
	 * @return the dokumentInfo
	 */
	public DokumentInfo getDokumentInfo() {
		return dokumentInfo;
	}
	
	/**
	 * Getter for the sporingsMetaData property.
	 * 
	 * @return the sporingsMetaData
	 */
	public SporingsMetaData getSporingsMetaData() {
		return sporingsMetaData;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("journalpostId", journalpostId).append("dokumentInfo", dokumentInfo)
				.toString();
	}

}
