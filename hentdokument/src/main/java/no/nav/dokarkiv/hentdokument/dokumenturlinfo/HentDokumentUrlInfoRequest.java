package no.nav.dokarkiv.hentdokument.dokumenturlinfo;

import static org.apache.logging.log4j.util.Strings.isBlank;

import lombok.Data;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;

/**
 * Request object for the HentDokumentUrlInfo.
 *
 * @author Magnus Skuland, Sirius IT
 */
@Data
public class HentDokumentUrlInfoRequest {

	private final String docToken;

	/**
	 * Validate that the request parameters are set.
	 */
	public void validate() {
		if (isBlank(docToken)) {
			throw new InvalidArgumentException("Missing parameter", "docToken", docToken);
		}
	}
}
