package no.nav.dokarkiv.hentdokument.dlf;

import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrl;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlRequest;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlResponse;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Implementation of VedleggUrlRetriever.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@Component
public class DefaultVedleggUrlRetriever implements VedleggUrlRetriever {

	private final HentDokumentUrl hentDokumentUrl;
	private final Long urlTimeToLive;
	private final boolean nonSslUrl;

	public DefaultVedleggUrlRetriever(HentDokumentUrl hentDokumentUrl,
									  @Value("${hentdokument.dlf.vedleggUrlTimeToLiveMinutes:480}") long urlTimeToLive,
									  @Value("${hentdokument.dlf.vedleggNonSslUrl:true}") boolean nonSslUrl) {
		this.hentDokumentUrl = hentDokumentUrl;
		this.urlTimeToLive = urlTimeToLive;
		this.nonSslUrl = nonSslUrl;
	}

	@Override
	public String retrieveVedleggUrl(String journalpostIdVedlegg, String filUuidVedlegg) {

		HentDokumentUrlRequest request = new HentDokumentUrlRequest(Long.valueOf(journalpostIdVedlegg), filUuidVedlegg, urlTimeToLive);

		HentDokumentUrlResponse response = null;
		try {
			response = hentDokumentUrl.hentDokumentUrlJoark(request);
		} catch (NoJournalpostFoundException | InvalidFilUuidException e) {
			throw new InvalidArgumentException("HentDokumentUrl for vedlegg failed", e);
		}
		return determineNonSslUrl(response);
	}

	/*
	 * Denne hacken er her fordi av historisk årsaker så har ikke DLF leseren klart å hente urler med https.
	 */
	private String determineNonSslUrl(HentDokumentUrlResponse response) {
		if(nonSslUrl) {
			return response.getDokumentUrl().replace("https:", "http:");
		} else {
			return response.getDokumentUrl();
		}
	}
}
