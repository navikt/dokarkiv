package no.nav.dokarkiv.core.consumer.sts;

import no.nav.dokarkiv.core.fasit.ServiceuserAlias;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;

import static no.nav.dokarkiv.core.cache.CacheConfig.REST_STS_CACHE;


@Component
public class StsRestConsumer {
	private static final String URL_ENCODED_BODY = "grant_type=client_credentials&scope=openid";

	private final RestTemplate restTemplate;
	private final String ststokenurl;

	public StsRestConsumer(@Value("${securitytokenservice.url}") String ststokenurl,
						   ServiceuserAlias serviceuserAlias,
						   RestTemplateBuilder restTemplateBuilder,
						   ClientHttpRequestFactory requestFactory) {
		this.ststokenurl = ststokenurl;
		this.restTemplate = restTemplateBuilder
				.requestFactory(() -> requestFactory)
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.setConnectTimeout(Duration.ofSeconds(3))
				.setReadTimeout(Duration.ofSeconds(20))
				.build();
	}

	@Retryable(include = StsException.class)
	@Cacheable(REST_STS_CACHE)
	public StsResponse getStsToken() {
		try {
			HttpHeaders headers = createHeaders();
			HttpEntity<String> requestEntity = new HttpEntity<>(URL_ENCODED_BODY, headers);

			return restTemplate.exchange(ststokenurl, HttpMethod.POST, requestEntity, StsResponse.class)
					.getBody();
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			throw new StsException(String.format("Klarte ikke hente token fra STS. Feilet med httpstatus=%s. Feilmelding=%s", e.getStatusCode(), e.getMessage()), e);
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		return headers;
	}
}