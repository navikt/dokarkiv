package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Eneste jobben denne skal gjøre er å tilby en retryable proxy for å hente dokumenturl.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
class RetryingJoarkHentDokumentFromUrlService {
	private final RestTemplate restTemplate;

	public RetryingJoarkHentDokumentFromUrlService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Retryable(HttpClientErrorException.class)
	byte[] hentDokumentFromJoark(final String dokumentUrl) {
		ResponseEntity<byte[]> forEntity = restTemplate.getForEntity(dokumentUrl, byte[].class);
		return forEntity.getBody();
	}
}
