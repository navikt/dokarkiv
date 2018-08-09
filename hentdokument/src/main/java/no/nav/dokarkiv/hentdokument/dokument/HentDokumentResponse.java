package no.nav.dokarkiv.hentdokument.dokument;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Response object for the service HentDokumentUrl.
 *
 * @author Thomas Eugen Bjørge, Sirius IT
 */
public class HentDokumentResponse {

	/**
	 * ID used for serialization.
	 */
	private static final long serialVersionUID = ***gammelt_fnr***16902996L;

	private byte[] dokument;

	/**
	 * Constructs a HentDokumentResponse.
	 *
	 * @param dokument The document.
	 */
	public HentDokumentResponse(byte[] dokument) {
		this.dokument = dokument;
	}

	/**
	 * Getter for the dokument property.
	 *
	 * @return the dokument
	 */
	public byte[] getDokument() {
		return dokument;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("Document size (b): ", dokument != null ? dokument.length : 0)
				.toString();
	}

}
