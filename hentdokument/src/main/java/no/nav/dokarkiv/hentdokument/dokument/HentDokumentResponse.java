package no.nav.dokarkiv.hentdokument.dokument;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Response object for the service HentDokumentUrl.
 *
 * @author Thomas Eugen Bjørge, Sirius IT
 */
public class HentDokumentResponse {
	private final byte[] dokument;

	/**
	 * Constructs a HentDokumentResponse.
	 *
	 * @param dokument The document.
	 */
	public HentDokumentResponse(byte[] dokument) {
		this.dokument = dokument.clone();
	}

	/**
	 * Getter for the dokument property.
	 *
	 * @return the dokument
	 */
	public byte[] getDokument() {
		return dokument.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("Document size (b): ", dokument == null ? 0 : dokument.length)
				.toString();
	}

}
