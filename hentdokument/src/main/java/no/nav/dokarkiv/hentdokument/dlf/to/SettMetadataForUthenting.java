package no.nav.dokarkiv.hentdokument.dlf.to;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;

/**
 * Scenario for henting av dokument for SettMetadataIDlf.
 *
 * @author Per Kristian Foss, Visma Consulting
 */
public class SettMetadataForUthenting implements Serializable {

	/**
	 * Serialization UID
	 */
	private static final long serialVersionUID = -619566855947887837L;

	private final Long journalpostId;
	private final String filUuid;
	private final Long versjon;

	/**
	 * Constructs a new SettMetadataForUthenting
	 *
	 * @param filUuid       The filUuid.
	 * @param versjon       The version.
	 * @param journalpostId The journalpostId
	 */
	public SettMetadataForUthenting(Long journalpostId, String filUuid, Long versjon) {
		this.journalpostId = journalpostId;
		this.filUuid = filUuid;
		this.versjon = versjon;
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
	 * Getter for the versjon property.
	 *
	 * @return the versjon
	 */
	public Long getVersjon() {
		return versjon;
	}

	/**
	 * Getter for the filUuid property.
	 *
	 * @return the filUuid
	 */
	public String getFilUuid() {
		return filUuid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this).toString();
	}
}
