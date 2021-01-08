package no.nav.dokarkiv.core.consumer.pdl;

import no.nav.dokarkiv.core.consumer.sts.StsRestConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static no.nav.dokarkiv.core.NavHeaders.BEARER_TOKEN_PREFIX;
import static no.nav.dokarkiv.core.cache.CacheConfig.HISTORISKE_IDENTER;
import static no.nav.dokarkiv.core.storage.RetryConstants.DELAY_SHORT;
import static no.nav.dokarkiv.core.storage.RetryConstants.MULTIPLIER_SHORT;

/**
 * PDL implementasjon av {@link IdentConsumer}
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class PdlIdentConsumer implements IdentConsumer {
	private static final String HEADER_PDL_NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";

	private final RestTemplate restTemplate;
	private final StsRestConsumer stsRestConsumer;
	private final URI pdlUri;

	public PdlIdentConsumer(@Value("${pdl.url}") String pdlUrl,
							RestTemplateBuilder restTemplateBuilder,
							StsRestConsumer stsRestConsumer) {
		this.restTemplate = restTemplateBuilder
				.setConnectTimeout(Duration.ofSeconds(3))
				.setReadTimeout(Duration.ofSeconds(20))
				.build();
		this.stsRestConsumer = stsRestConsumer;
		this.pdlUri = UriComponentsBuilder.fromHttpUrl(pdlUrl).build().toUri();
	}

	@Retryable(
			include = HttpServerErrorException.class,
			backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT)
	)
	@Override
	public String hentAktoerId(String folkeregisterIdent) throws PersonIkkeFunnetException {
		try {
			final RequestEntity<PdlRequest> requestEntity = baseRequest()
					.body(mapHentAktoerIdForFolkeregisterident(folkeregisterIdent));
			final PdlResponse pdlResponse = requireNonNull(restTemplate.exchange(requestEntity, PdlResponse.class).getBody());

			if (pdlResponse.getErrors() == null || pdlResponse.getErrors().isEmpty()) {
				return pdlResponse.getData().getHentIdenter().getIdenter().get(0).getIdent();
			} else {
				if (PERSON_IKKE_FUNNET_CODE.equals(pdlResponse.getErrors().get(0).getExtensions().getCode())) {
					throw new PersonIkkeFunnetException("Fant ikke aktørid for person i pdl.");
				}
				throw new PdlFunctionalException("Kunne ikke hente aktørid for folkeregisterident i pdl. " + pdlResponse.getErrors());
			}
		} catch (HttpClientErrorException e) {
			throw new PdlFunctionalException("Kall mot pdl feilet funksjonelt.", e);
		}
	}

	private PdlRequest mapHentAktoerIdForFolkeregisterident(final String ident) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("ident", ident);
		return PdlRequest.builder()
				.query("query hentIdenter($ident: ID!) {hentIdenter(ident: $ident, grupper: AKTORID, historikk: false) {identer { ident gruppe historisk } } }")
				.variables(variables)
				.build();
	}

	@Retryable(
			include = HttpServerErrorException.class,
			backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT)
	)
	@Override
	public String hentFolkeregisterIdent(String aktoerId) throws PersonIkkeFunnetException {
		try {
			final RequestEntity<PdlRequest> requestEntity = baseRequest()
					.body(mapHentFolkeregisterIdentForAktoerId(aktoerId));
			final PdlResponse pdlResponse = requireNonNull(restTemplate.exchange(requestEntity, PdlResponse.class).getBody());

			if (pdlResponse.getErrors() == null || pdlResponse.getErrors().isEmpty()) {
				return pdlResponse.getData().getHentIdenter().getIdenter().get(0).getIdent();
			} else {
				if (PERSON_IKKE_FUNNET_CODE.equals(pdlResponse.getErrors().get(0).getExtensions().getCode())) {
					throw new PersonIkkeFunnetException("Fant ikke folkeregisterident for person i pdl.");
				}
				throw new PdlFunctionalException("Kunne ikke hente folkeregisterident for aktørid i pdl. " + pdlResponse.getErrors());
			}
		} catch (HttpClientErrorException e) {
			throw new PdlFunctionalException("Kall mot pdl feilet funksjonelt.", e);
		}
	}

	private PdlRequest mapHentFolkeregisterIdentForAktoerId(final String ident) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("ident", ident);
		return PdlRequest.builder()
				.query("query hentIdenter($ident: ID!) {hentIdenter(ident: $ident, grupper: FOLKEREGISTERIDENT, historikk: false) {identer { ident gruppe historisk } } }")
				.variables(variables)
				.build();
	}

	@Cacheable(HISTORISKE_IDENTER)
	@Retryable(
			include = HttpServerErrorException.class,
			backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT)
	)
	@Override
	public List<String> hentHistoriskeFolkeregisterIdenter(String folkeregisterIdent) throws PersonIkkeFunnetException {
		try {
			final RequestEntity<PdlRequest> requestEntity = baseRequest()
					.body(mapHentHistoriskeFolkeregisterIdentForAktoerId(folkeregisterIdent));
			final PdlResponse pdlResponse = requireNonNull(restTemplate.exchange(requestEntity, PdlResponse.class).getBody());

			if (pdlResponse.getErrors() == null || pdlResponse.getErrors().isEmpty()) {
				return pdlResponse.getData().getHentIdenter().getIdenter().stream().map(PdlResponse.PdlIdent::getIdent).collect(Collectors.toList());
			} else {
				if (PERSON_IKKE_FUNNET_CODE.equals(pdlResponse.getErrors().get(0).getExtensions().getCode())) {
					throw new PersonIkkeFunnetException("Fant ikke historiske identer for person i pdl.");
				}
				throw new PdlFunctionalException("Kunne ikke hente historiske identer for ident." + pdlResponse.getErrors());
			}
		} catch (HttpClientErrorException e) {
			throw new PdlFunctionalException("Kall mot pdl feilet funksjonelt.", e);
		}
	}

	private PdlRequest mapHentHistoriskeFolkeregisterIdentForAktoerId(final String ident) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("ident", ident);
		return PdlRequest.builder()
				.query("query hentIdenter($ident: ID!) {hentIdenter(ident: $ident, grupper: FOLKEREGISTERIDENT, historikk: true) {identer { ident gruppe historisk } } }")
				.variables(variables)
				.build();
	}

	private RequestEntity.BodyBuilder baseRequest() {
		final String serviceuserToken = stsRestConsumer.getStsToken().getAccess_token();
		return RequestEntity.post(pdlUri)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_PREFIX + serviceuserToken)
				.header(HEADER_PDL_NAV_CONSUMER_TOKEN, BEARER_TOKEN_PREFIX + serviceuserToken);
	}
}
