package no.nav.dokarkiv.core.dokumenturl;

import lombok.Data;

/**
 * Response object for the service HentDokumentUrl.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 */
@Data
public class HentDokumentUrlResponse {
	private final String dokumentUrl;
}
