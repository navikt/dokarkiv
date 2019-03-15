package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlResponse;
import no.nav.dokarkiv.core.ondemand.HentOndemandDokument;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;

/**
 * Henter dokumenter fra joark (special case)
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class SafHentDokumentJoarkRepository {
	private final RestTemplate restTemplate;
	private final HentOndemandDokument hentOndemandDokument;

	@Inject
	SafHentDokumentJoarkRepository(RestTemplate restTemplate,
								   HentOndemandDokument hentOndemandDokument) {
		this.restTemplate = restTemplate;
		this.hentOndemandDokument = hentOndemandDokument;
	}

	byte[] hentDokument(final JoarkDokumentDto joarkDokumentDto) {
		HentDokumentUrlResponse dokumentUrl = hentOndemandDokument.createDokumentUrl(joarkDokumentDto.getJournalpostId(), joarkDokumentDto.getFilUuid());
		ResponseEntity<byte[]> forEntity = restTemplate.getForEntity(dokumentUrl.getDokumentUrl(), byte[].class);
		return forEntity.getBody();
	}
}
