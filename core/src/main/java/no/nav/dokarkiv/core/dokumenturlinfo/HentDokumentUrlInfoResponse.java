package no.nav.dokarkiv.core.dokumenturlinfo;

import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Response object for the HentDokumentUrlInfo.
 *
 * @author Magnus Skuland, Sirius IT
 */
public class HentDokumentUrlInfoResponse implements Serializable {

	private static final long serialVersionUID = -***gammelt_fnr***05143913L;

	private DokumentUrlInfo dokumentUrl;

	/**
	 * Constructs a HentDokumentUrlInfoResponse object
	 *
	 * @param dokumentUrlInfo domain object
	 */
	public HentDokumentUrlInfoResponse(DokumentUrlInfo dokumentUrlInfo) {
		super();
		this.dokumentUrl = dokumentUrlInfo;
	}

	/**
	 * Getter for the dokumentUrlInfo property.
	 *
	 * @return the dokumentUrl
	 */
	public DokumentUrlInfo getDokumentUrl() {
		return dokumentUrl;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("dokumentUrl", dokumentUrl).toString();
	}

}
