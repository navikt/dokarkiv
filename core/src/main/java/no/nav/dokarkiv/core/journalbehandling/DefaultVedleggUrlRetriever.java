package no.nav.dokarkiv.core.journalbehandling;

import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrl;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlRequest;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlResponse;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import org.springframework.stereotype.Component;

/**
 * Implementation of VedleggUrlRetriever.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@Component
public class DefaultVedleggUrlRetriever implements VedleggUrlRetriever {

	private HentDokumentUrl hentDokumentUrl;
	private Long urlTimeToLive;
	private Boolean nonSSLUrl;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String retrieveVedleggUrl(String journalpostIdVedlegg, String filUuidVedlegg) {

		HentDokumentUrlRequest request = new HentDokumentUrlRequest(Long.valueOf(journalpostIdVedlegg), filUuidVedlegg,
				urlTimeToLive); //nonSSLUrl

		HentDokumentUrlResponse response = null;
		try {
			response = hentDokumentUrl.hentDokumentUrl(request);
		} catch (NoJournalpostFoundException e) {
			throw new InvalidArgumentException("HentDokumentUrl for vedlegg failed", e);
		} catch (InvalidFilUuidException e) {
			throw new InvalidArgumentException("HentDokumentUrl for vedlegg failed", e);
		}
		return response.getDokumentUrl();
	}

	/**
	 * Setter for the hentDokumentUrl property.
	 *
	 * @param hentDokumentUrl the hentDokumentUrl to set
	 */
	public void setHentDokumentUrl(HentDokumentUrl hentDokumentUrl) {
		this.hentDokumentUrl = hentDokumentUrl;
	}

	/**
	 * Setter for the urlTimeToLive property.
	 *
	 * @param urlTimeToLive the urlTimeToLive to set
	 */
	public void setUrlTimeToLive(Long urlTimeToLive) {
		this.urlTimeToLive = urlTimeToLive;
	}

	/**
	 * Setter for the nonSSLUrl property.
	 *
	 * @param nonSSLUrl the nonSSLUrl to set
	 */
	public void setNonSSLUrl(Boolean nonSSLUrl) {
		this.nonSSLUrl = nonSSLUrl;
	}

}
