package no.nav.dokarkiv.core.consumer.gsak.hentgsaksaker;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.cache.CacheConfig;
import no.nav.dokarkiv.core.consumer.gsak.domain.SakInfoTo;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.fasit.ServiceuserAlias;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Slf4j
@Component
public class GsakConsumer {

	private static final int TIMEOUT = 10_000;
	private final RestTemplate restTemplate;
	private final String gsakApiUrl;

	public GsakConsumer(RestTemplateBuilder restTemplateBuilder,
						@Value("${sak.saker.url}") String gsakApiUrl,
						ServiceuserAlias serviceuserAlias) {
		this.gsakApiUrl = gsakApiUrl;
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(TIMEOUT)
				.setConnectTimeout(TIMEOUT)
				.basicAuthorization(serviceuserAlias.getUsername(), serviceuserAlias.getPassword()).build();
	}

	@Cacheable(cacheNames = CacheConfig.GSAK_SAK_CACHE)
	public SakInfoTo hentSakInfo(final String sakId) {
		String url = gsakApiUrl + "/" + sakId;
		return hentSakInfo(url);
	}

	private SakInfoTo hentSaker(final String uri) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("X-Correlation-ID", UUID.randomUUID().toString());
			ResponseEntity<SakInfoTo> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), SakInfoTo.class);
			return response.getBody();
		} catch (HttpServerErrorException e) {
			throw new DokarkivTechnicalException(String.format("getGsaksaker feilet teknisk med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e);
		} catch (HttpClientErrorException e) {
			throw new DokarkivFunctionalException(String.format("getGsaksaker feilet funksjonelt med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e);
		}
	}
}