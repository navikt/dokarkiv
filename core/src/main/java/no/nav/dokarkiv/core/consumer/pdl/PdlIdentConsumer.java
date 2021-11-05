package no.nav.dokarkiv.core.consumer.pdl;

import no.nav.dokarkiv.core.consumer.sts.StsRestConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.RequestEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
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
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * PDL implementasjon av {@link IdentConsumer}
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class PdlIdentConsumer implements IdentConsumer {
	private static final String HEADER_PDL_NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";
	private static final String TEMA = "Tema";

	private final RestTemplate restTemplate;
	private final StsRestConsumer stsRestConsumer;
	private final URI pdlUri;

	@Inject
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
					.body(mapHentAktoerIdForFolkeregisterident(this.validateFolkeregisterIdent(folkeregisterIdent)));
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
					.body(mapHentFolkeregisterIdentForAktoerId(this.validateFolkeregisterIdent(aktoerId)));
			final PdlResponse pdlResponse = requireNonNull(restTemplate.exchange(requestEntity, PdlResponse.class).getBody());

			if (pdlResponse.getErrors() == null || pdlResponse.getErrors().isEmpty()) {
				if(pdlResponse.getData().getHentIdenter().getIdenter().isEmpty())
					throw new PersonIngenIdentFunnetException("Ingen identer ble funnet for personen i pdl.");
				else
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
					.body(mapHentHistoriskeFolkeregisterIdentForAktoerId(this.validateFolkeregisterIdent(folkeregisterIdent)));
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

	@Override
	public String hentPersonIdent(String aktoerId, String tema) {
		try {
			final RequestEntity<PdlRequest> requestEntity = temaRequest(tema)
					.body(mapHentPersonIdentForId(this.validateFolkeregisterIdent(aktoerId)));
			final PdlPersonResponse pdlPersonResponse = requireNonNull(restTemplate.exchange(requestEntity, PdlPersonResponse.class).getBody());

			if (pdlPersonResponse.getData().getHentPerson() != null && !pdlPersonResponse.getData().getHentPerson().getNavn().isEmpty()) {
				return pdlPersonResponse.getData().getHentPerson().getNavn().get(0).getNavn();
			} else {
				if (PERSON_IKKE_FUNNET_CODE.equals(pdlPersonResponse.getErrors().get(0).getExtensions().getCode())) {
					throw new PersonIkkeFunnetException("Fant ikke navn for person i pdl.");
				}
				throw new PdlFunctionalException("Kunne ikke hente navn for aktørid i pdl. " + pdlPersonResponse.getErrors());
			}
		} catch (HttpClientErrorException e) {
			throw new PdlFunctionalException("Kall mot pdl feilet funksjonelt.", e);
		}
	}

	private PdlRequest mapHentPersonIdentForId(final String ident) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("ident", ident);
		return PdlRequest.builder()
				.query("query hentPerson($ident: ID!) {hentPerson(ident: $ident) {\n" +
						"navn(historikk: false) {\n" +
						"  fornavn\n" +
						"  mellomnavn\n" +
						"  etternavn\n" +
						"}" +
						"}}")
				.variables(variables)
				.build();
	}

	private PdlRequest mapHentHistoriskeFolkeregisterIdentForAktoerId(final String ident) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("ident", ident);
		return PdlRequest.builder()
				.query("query hentIdenter($ident: ID!) {hentIdenter(ident: $ident, grupper: FOLKEREGISTERIDENT, historikk: true) {identer { ident gruppe historisk } } }")
				.variables(variables)
				.build();
	}

	private RequestEntity.BodyBuilder temaRequest(String tema) {
		final String serviceuserToken = stsRestConsumer.getStsToken().getAccess_token();
		return RequestEntity.post(pdlUri)
				.accept(APPLICATION_JSON)
				.header(TEMA, tema)
				.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.header(AUTHORIZATION, BEARER_TOKEN_PREFIX + serviceuserToken)
				.header(HEADER_PDL_NAV_CONSUMER_TOKEN, BEARER_TOKEN_PREFIX + serviceuserToken);
	}

	private RequestEntity.BodyBuilder baseRequest() {
		final String serviceuserToken = stsRestConsumer.getStsToken().getAccess_token();
		return RequestEntity.post(pdlUri)
				.accept(APPLICATION_JSON)
				.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.header(AUTHORIZATION, BEARER_TOKEN_PREFIX + serviceuserToken)
				.header(HEADER_PDL_NAV_CONSUMER_TOKEN, BEARER_TOKEN_PREFIX + serviceuserToken);
	}

	String validateFolkeregisterIdent(String ident) {
		if (isBlank(ident)) {
			throw new PersonIkkeFunnetException("Validering av ident feilet fordi verdien er null eller blank.");
		}

		String identTrimed = ident.trim();

		if (!isNumeric(identTrimed)) {
			throw new PersonIkkeFunnetException("Validering av ident feilet fordi verdien inneholder bokstaver");
		}

		if (identTrimed.length() != 13 && identTrimed.length() != 11) {
			throw new PersonIkkeFunnetException("Validering av ident feilet fordi verdien har lengde " + identTrimed.length() + ". Akseptert lengde er 11 eller 13");
		}

		return identTrimed;
	}
}
