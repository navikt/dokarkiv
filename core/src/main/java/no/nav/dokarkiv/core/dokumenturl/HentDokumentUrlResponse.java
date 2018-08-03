package no.nav.dokarkiv.core.dokumenturl;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Response object for the service HentDokumentUrl.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 */
public class HentDokumentUrlResponse {

	/**
	 * ID used for serialization.
	 */
	private static final long serialVersionUID = -***gammelt_fnr***73158631L;

	private String dokumentUrl;

	/**
	 * Constructs a HentDokumentUrlResponse
	 * 
	 * @param dokumentUrl
	 *            The URL of the document.
	 */
	public HentDokumentUrlResponse(String dokumentUrl) {
		this.dokumentUrl = dokumentUrl;
	}

	/**
	 * Getter for the dokumentUrl property.
	 * 
	 * @return the dokumentUrl
	 */
	public String getDokumentUrl() {
		return dokumentUrl;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return new ToStringBuilder(this)
		.append("dokumentUrl", dokumentUrl)
		.toString();
	}
	
	
}
