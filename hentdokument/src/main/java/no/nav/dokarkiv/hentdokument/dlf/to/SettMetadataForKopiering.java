package no.nav.dokarkiv.hentdokument.dlf.to;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;

/**
 * Scenario for kopiering av dokument som redigerbart vedlegg for SettMetadataIDlf.
 *
 * @author Per Kristian Foss, Visma Consulting
 */
public class SettMetadataForKopiering implements Serializable {

	/**
	 * Serialization UID
	 */
	private static final long serialVersionUID = ***gammelt_fnr***59271019L;

	private Long journalpostIdVedlegg;
	private String filUuidVedlegg;
	private byte[] dlfHoveddokument;

	/**
	 * Constructs a new SettMetadataForKopiering.
	 *
	 * @param journalpostIdVedlegg The journalpostId of the vedlegg Journalpost.
	 * @param filUuidVedlegg       The filUuid of the vedlegg.
	 * @param dlfHoveddokument     The hoveddokument containing envrionment configuration.
	 */
	public SettMetadataForKopiering(Long journalpostIdVedlegg, String filUuidVedlegg, byte[] dlfHoveddokument) {
		this.journalpostIdVedlegg = journalpostIdVedlegg;
		this.filUuidVedlegg = filUuidVedlegg;
		this.dlfHoveddokument = dlfHoveddokument == null ? null : dlfHoveddokument.clone();
	}

	/**
	 * Getter for the journalpostIdVedlegg property.
	 *
	 * @return the journalpostIdVedlegg
	 */
	public Long getJournalpostIdVedlegg() {
		return journalpostIdVedlegg;
	}

	/**
	 * Getter for the filUuidVedlegg property.
	 *
	 * @return the filUuidVedlegg
	 */
	public String getFilUuidVedlegg() {
		return filUuidVedlegg;
	}

	/**
	 * Getter for the dlfHoveddokument property.
	 *
	 * @return the dlfHoveddokument
	 */
	public byte[] getDlfHoveddokument() {
		return dlfHoveddokument == null ? null : dlfHoveddokument.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this).toString();
	}
}
