package no.nav.dokarkiv.journal.v3.tjoark050;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Response object for HentDokumentUrl.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class HentDokumentUrlResponseTo {

	private String dokumentUrl;

	/**
	 * Constructs a HentDokumentUrlResponseTo
	 * 
	 * @param dokumentUrl The URL of the document.
	 */
	public HentDokumentUrlResponseTo(String dokumentUrl) {
		this.dokumentUrl = dokumentUrl;
	}

	public String getDokumentUrl() {
		return dokumentUrl;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
		.append("dokumentUrl", dokumentUrl)
		.toString();
	}

}
