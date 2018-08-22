package no.nav.dokarkiv.hentdokument.dlf.to;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Response object for service TJOARK039 SettMetadataIDLF
 *
 * @author Per Kristian Foss, Visma Sirius
 */
public class SettMetadataIDLFResponse {

	/**
	 * ID used for serialization.
	 */
	private static final long serialVersionUID = ***gammelt_fnr***53399069L;

	private byte[] dlfDokument;

	/**
	 * Constructs a new SettMetadataIDLFResponse.
	 */
	public SettMetadataIDLFResponse() {
	}

	/**
	 * Constructs a new SettMetadataIDLFResponse.
	 *
	 * @param dlfDokument The updated DLF-document
	 */
	public SettMetadataIDLFResponse(byte[] dlfDokument) {
		this.dlfDokument = dlfDokument == null ? null : dlfDokument.clone();
	}

	/**
	 * Getter for the dlfDokument property
	 *
	 * @return the dlfDokument
	 */
	public byte[] getDlfDokument() {
		return dlfDokument == null ? null : dlfDokument.clone();
	}

	/**
	 * Setter for the dlfDokument property
	 *
	 * @param dlfDokument the dlfDokument to set
	 */
	public void setDlfDokument(byte[] dlfDokument) {
		this.dlfDokument = dlfDokument == null ? null : dlfDokument.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this).toString();
	}
}
