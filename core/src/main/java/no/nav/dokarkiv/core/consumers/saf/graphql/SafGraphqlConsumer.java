package no.nav.dokarkiv.core.consumers.saf.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.consumers.saf.journalpost.SafJournalpostTo;
import no.nav.dokarkiv.core.consumers.saf.journalpost.SafJsonJournalpost;
import no.nav.dokarkiv.core.exceptions.JsonParserTechnicalException;
import no.nav.dokarkiv.core.exceptions.saf.SafJournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.saf.SafJournalpostQueryTechnicalException;
import no.nav.dokarkiv.core.exceptions.saf.SafJournalpostUnauthorizedException;
import no.nav.dokarkiv.core.exceptions.ValidationFunctionalException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CALL_ID;
import static no.nav.dokarkiv.core.storage.RetryConstants.DELAY_SHORT;
import static no.nav.dokarkiv.core.storage.RetryConstants.MAX_ATTEMPTS_SHORT;


@Component
@Slf4j
public class SafGraphqlConsumer {

	private static final String OIDC_TOKEN_PREFIX = "Bearer";
	private final RestTemplate restTemplate;
	private final String graphQLurl;

	@Autowired
	public SafGraphqlConsumer(RestTemplateBuilder restTemplateBuilder,
							  @Value("${saf.graphql.url}") String graphQLurl) {
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.build();
		this.graphQLurl = graphQLurl;
	}


	@RestMetrics(value = "dok_request", extraTags = {"process_code", "safJournalpostQuery"}, percentiles = {0.5, 0.95})
	@Retryable(include = SafJournalpostQueryTechnicalException.class, maxAttempts = MAX_ATTEMPTS_SHORT, backoff = @Backoff(delay = DELAY_SHORT))
	public SafJournalpostTo performQuery(GraphQLRequest graphQLRequest, String authorizationHeader, String journalpostId) {

		try {
			HttpHeaders httpHeaders = createAuthHeaderFromToken(authorizationHeader, journalpostId);
			if (MDC.get(MDC_CALL_ID) != null) {
				httpHeaders.add("X-Correlation-ID", MDC.get(MDC_CALL_ID));
			}

			ResponseEntity<SafJsonJournalpost> responseEntity = restTemplate.exchange(graphQLurl, HttpMethod.POST, new HttpEntity<>(requestToJson(graphQLRequest, journalpostId), httpHeaders), SafJsonJournalpost.class);

			if (responseEntity.getBody() == null || responseEntity.getBody().getData() == null || responseEntity.getBody()
					.getData().getJournalpost() == null) {
				throw new SafJournalpostIkkeFunnetException(String.format("Ingen journalpost ble funnet for journalpostId=%s", journalpostId));
			}

			return responseEntity.getBody().getJournalpost();
		} catch (HttpClientErrorException e) {
			throw new SafJournalpostUnauthorizedException(format("Henting av journalpost feilet med status: %s, feilmelding: %s", e
					.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new SafJournalpostQueryTechnicalException(format("Tjenesten SAF (graphQL) feilet med status: %s, feilmelding: %s", e.getStatusCode(), e.getMessage()), e);
		}
	}

	private HttpHeaders createAuthHeaderFromToken(String authorizationHeader, String journalpostId) {
		if (!OIDC_TOKEN_PREFIX.equals(authorizationHeader.split(" ")[0])) {
			throw new ValidationFunctionalException(String.format("Authorization header må være på formen Bearer {token} for journalpostId=%s", journalpostId));
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(authorizationHeader);
		return headers;
	}

	private String requestToJson(GraphQLRequest graphQLRequest, String journalpostId) {
		try {
			return new ObjectMapper().writeValueAsString(graphQLRequest);
		} catch (JsonProcessingException e) {
			throw new JsonParserTechnicalException(String.format("Kunne ikke konvertere graphQlRequest til json for journalpostId=%s, feilmelding=%s", journalpostId, e
					.getMessage()), e);
		}
	}
}
