package no.nav.dokarkiv.core.consumer.gsak.hentgsaksaker;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CALL_ID;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.GsakHentSakFunctionalException;
import no.nav.dokarkiv.core.exceptions.GsakHentSakTechnicalException;
import no.nav.dokarkiv.core.exceptions.GsakOpprettSakFunctionalException;
import no.nav.dokarkiv.core.exceptions.GsakOpprettSakTechnicalException;
import no.nav.dokarkiv.core.fasit.ServiceuserAlias;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class GsakConsumer {

	private final RestTemplate restTemplate;
	private final String gsakApiUrl;

	public GsakConsumer(RestTemplateBuilder restTemplateBuilder,
                        @Value("${sak_saker_url}") String gsakApiUrl,
                        ServiceuserAlias serviceuserAlias) {
		this.gsakApiUrl = gsakApiUrl;
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword()).build();
	}

	public List<GsakSakerTo> getSakerMatchingRequest(GsakRequestTo request) {
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(gsakApiUrl)
				.queryParam("aktoerId", request.getAktoerId())
				.queryParam("orgnr", request.getOrgnr())
				.queryParam("tema", request.getTema())
				.queryParam("fagsakNr", request.getFagsakNr())
				.queryParam("applikasjon", request.getApplikasjon());
		return hentSaker(uri.toUriString());
	}

	public GsakSakerTo opprettSak(GsakRequestTo request) {
		if (log.isDebugEnabled()) {
			log.debug("Oppretter ny gsak");
		}
		try {
			GsakPostRequest gsakPostRequest = GsakPostRequest.builder()
					.tema(request.getTema())
					.aktoerId(request.getAktoerId())
					.orgnr(request.getOrgnr())
					.fagsakNr(request.getFagsakNr())
					.applikasjon(request.getApplikasjon())
					.build();

			HttpHeaders headers = new HttpHeaders();
			headers.set("X-Correlation-ID", MDC.get(MDC_CALL_ID));
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<GsakPostRequest> requestEntity = new HttpEntity<>(gsakPostRequest, headers);

			ResponseEntity<GsakSakerTo> response = restTemplate.postForEntity(gsakApiUrl, requestEntity, GsakSakerTo.class);
			if (log.isDebugEnabled()) {
				log.debug("Gsak med id={} opprettet", response.getBody().getId());
			}
			return response.getBody();

		} catch (HttpClientErrorException e) {
			throw new GsakOpprettSakFunctionalException(String.format("getGsaksaker feilet funksjonelt med statuskode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new GsakOpprettSakTechnicalException(String.format("getGsaksaker feilet teknisk med statuskode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e);
		}
	}

	private List<GsakSakerTo> hentSaker(final String uri) {
		if (log.isDebugEnabled()) {
			log.debug("Henter gsaker uri={}", uri);
		}
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("X-Correlation-ID", MDC.get(MDC_CALL_ID));
			ResponseEntity<List<GsakSakerTo>> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<List<GsakSakerTo>>() {
			});
			if (log.isDebugEnabled()) {
				if (response.getBody().isEmpty()) {
					log.debug("Tom respons for uri={}", uri);
				} else {
					log.debug("Hentet ferdig gsaker uri={}", uri);
				}
			}
			return response.getBody();
		} catch (HttpClientErrorException e) {
			throw new GsakHentSakFunctionalException(String.format("getGsaksaker feilet funksjonelt med statuskode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new GsakHentSakTechnicalException(String.format("getGsaksaker feilet teknisk med statuskode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e);
		}
	}
}
