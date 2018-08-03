package no.nav.dokarkiv.core.dokumenturlinfo;

/**
 * Defines the contract for OnDemand-related functionality.
 * 
 * @author Magnus Skuland, Sirius IT
 * @author Eirik Bergande, Sirius IT
 */
public interface HentDokumentUrlInfo {

	/**
	 * Returns a HentDokumentUrlInfoResponse based on the given request.
	 * 
	 * @param hentDokumentUrlInfoRequest
	 *            Request containing an url
	 * @return A HentDokumentUrlInfoResponse containing a DokumentUrl
	 */
	HentDokumentUrlInfoResponse hentDokumentUrlInfo(HentDokumentUrlInfoRequest hentDokumentUrlInfoRequest);

}
