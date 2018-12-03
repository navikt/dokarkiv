package no.nav.dokarkiv.core.ondemand;

import io.micrometer.core.annotation.Timed;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrl;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlRequest;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlResponse;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class HentOndemandDokument {
	private final RestTemplate restTemplate;
	private final HentDokumentUrl hentDokumentUrl;

	@Inject
	public HentOndemandDokument(RestTemplate restTemplate, HentDokumentUrl hentDokumentUrl) {
		this.restTemplate = restTemplate;
		this.hentDokumentUrl = hentDokumentUrl;
	}

	@Timed(value = "dok_consumer_request", extraTags = {"process_code", "hentOnDemandDokument"}, percentiles = {0.5, 0.95})
	public byte[] hentOndemandDokumentFromJoark(String dokumentUrl) {
		return restTemplate.getForObject(dokumentUrl, byte[].class);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public HentDokumentUrlResponse createDokumentUrl(Long journalpostId, String filUuid) throws InvalidFilUuidException, NoJournalpostFoundException {
		return hentDokumentUrl.hentDokumentUrlJoark(mapHentDokumentRequest(journalpostId, filUuid));
	}

	private HentDokumentUrlRequest mapHentDokumentRequest(Long journalpostId, String filUuid) {
		return new HentDokumentUrlRequest(journalpostId, filUuid, 1L);
	}
}
