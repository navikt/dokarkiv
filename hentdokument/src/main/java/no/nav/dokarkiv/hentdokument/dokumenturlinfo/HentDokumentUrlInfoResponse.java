package no.nav.dokarkiv.hentdokument.dokumenturlinfo;

import lombok.Data;
import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;

/**
 * Response object for the HentDokumentUrlInfo.
 *
 * @author Magnus Skuland, Sirius IT
 */
@Data
public class HentDokumentUrlInfoResponse {
	private final DokumentUrlInfo dokumentUrl;
}
