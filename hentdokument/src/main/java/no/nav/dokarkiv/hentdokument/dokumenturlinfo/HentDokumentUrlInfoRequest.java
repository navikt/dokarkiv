package no.nav.dokarkiv.hentdokument.dokumenturlinfo;

import static org.apache.logging.log4j.util.Strings.isBlank;

import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Request object for the HentDokumentUrlInfo.
 *
 * @author Magnus Skuland, Sirius IT
 */
public class HentDokumentUrlInfoRequest {

	private static final long serialVersionUID = ***gammelt_fnr***94300753L;

	private String docToken;

	/**
	 * Constructs a {@link HentDokumentUrlInfoRequest}.
	 *
	 * @param docToken part of the url
	 */
	public HentDokumentUrlInfoRequest(String docToken) {
		this.docToken = docToken;
	}

	/**
	 * Validate that the request parameters are set.
	 */
	public void validate() {
		if (isBlank(docToken)) {
			throw new InvalidArgumentException("Missing parameter", "docToken", docToken);
		}
	}

	/**
	 * Getter for the docToken property.
	 *
	 * @return the docToken
	 */
	public String getDocToken() {
		return docToken;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("docToken", docToken).toString();
	}

}
