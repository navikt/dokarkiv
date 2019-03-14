package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlResponse;
import no.nav.dokarkiv.core.ondemand.HentOndemandDokument;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class SafHentDokumentOndemandRepository {
	private final RestTemplate restTemplate;
	private final HentOndemandDokument hentOndemandDokument;

	@Inject
	SafHentDokumentOndemandRepository(RestTemplate restTemplate,
									  HentOndemandDokument hentOndemandDokument) {
		this.restTemplate = restTemplate;
		this.hentOndemandDokument = hentOndemandDokument;
	}

	byte[] hentDokumentFromOndemand(final JoarkDokumentDto joarkDokumentDto) {
		HentDokumentUrlResponse dokumentUrl = hentOndemandDokument.createDokumentUrl(joarkDokumentDto.getJournalpostId(), joarkDokumentDto.getFilUuid());
		ResponseEntity<byte[]> forEntity = restTemplate.getForEntity(dokumentUrl.getDokumentUrl(), byte[].class);
		return forEntity.getBody();
	}
}
