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

import javax.inject.Inject;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static no.nav.dokarkiv.core.NavHeaders.BEARER_TOKEN_PREFIX;
import static no.nav.dokarkiv.core.cache.CacheConfig.HISTORISKE_IDENTER;
import static no.nav.dokarkiv.core.cache.CacheConfig.PERSON_IDENTER;
import static no.nav.dokarkiv.core.storage.RetryConstants.DELAY_SHORT;
import static no.nav.dokarkiv.core.storage.RetryConstants.MULTIPLIER_SHORT;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

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

	@Cacheable(PERSON_IDENTER)
	@Retryable(
			include = HttpServerErrorException.class,
			backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT)
	)
	@Override
	public PersonIdent hentAktoer(String folkeregisterIdent, String tema) throws PersonIkkeFunnetException {
		try {
			final RequestEntity<PdlRequest> requestEntity = temaRequest(tema)
					.body(mapHentAktoerIdAndNavnForFolkeregisterident(this.validateFolkeregisterIdent(folkeregisterIdent)));
			final PdlResponse pdlResponse = requireNonNull(restTemplate.exchange(requestEntity, PdlResponse.class).getBody());

			if (pdlResponse.getData() != null && pdlResponse.getData().getHentIdenter()!=null && !pdlResponse.getData().getHentIdenter().getIdenter().isEmpty()) {
				return mapToPersonIdent(pdlResponse.getData());
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

	private PersonIdent mapToPersonIdent(PdlResponse.PdlHentIdenter data) {
		if(data.getHentPerson()!=null && data.getHentPerson().getNavn()!=null && !data.getHentPerson().getNavn().isEmpty()){
			PdlResponse.PdlNavn navn = data.getHentPerson().getNavn().get(0);
			return PersonIdent.builder().fornavn(navn.getFornavn()).mellomnavn(navn.getMellomnavn()).etternavn(navn.getEtternavn()).ident(data.getHentIdenter().getIdenter().get(0).getIdent()).build();
		}
		return PersonIdent.builder().ident(data.getHentIdenter().getIdenter().get(0).getIdent()).build();
	}

	private PdlRequest mapHentAktoerIdAndNavnForFolkeregisterident(final String ident) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("ident", ident);
		return PdlRequest.builder()
				.query("query hentIdenter($ident: ID!) {hentIdenter(ident: $ident, grupper: AKTORID, historikk: false) {identer { ident gruppe historisk } }," +
						", hentPerson(ident: $ident) {\n" +
						"\tnavn(historikk: false) {\n" +
						"\t  fornavn\n" +
						"\t  mellomnavn\n" +
						"\t  etternavn\n" +
						"    }\n" +
						"  } }")
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

	private PdlRequest mapHentHistoriskeFolkeregisterIdentForAktoerId(final String ident) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("ident", ident);
		return PdlRequest.builder()
				.query("query hentIdenter($ident: ID!) {hentIdenter(ident: $ident, grupper: FOLKEREGISTERIDENT, historikk: true) {identer { ident gruppe historisk } }, " +
						", hentPerson(ident: $ident) {\n" +
						"\tnavn(historikk: false) {\n" +
						"\t  fornavn\n" +
						"\t  mellomnavn\n" +
						"\t  etternavn\n" +
						"    }\n" +
						"  } }")
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

	private RequestEntity.BodyBuilder temaRequest(String tema) {
		final String serviceuserToken = stsRestConsumer.getStsToken().getAccess_token();
		return RequestEntity.post(pdlUri)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_PREFIX + serviceuserToken)
				.header(TEMA, tema)
				.header(HEADER_PDL_NAV_CONSUMER_TOKEN, BEARER_TOKEN_PREFIX + serviceuserToken);
	}

	String validateFolkeregisterIdent(String ident) {
		if(isBlank(ident)) {
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
