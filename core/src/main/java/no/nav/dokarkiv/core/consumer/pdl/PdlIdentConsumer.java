package no.nav.dokarkiv.core.consumer.pdl;

import no.nav.dokarkiv.core.consumer.azure.AzureToken;
import no.nav.dokarkiv.core.exceptions.AzureTokenException;
import no.nav.dokarkiv.core.properties.DokarkivProperties;
import no.nav.dokarkiv.core.util.NavHeadersFilter;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.cache.CacheConfig.HISTORISKE_IDENTER;
import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.core.storage.RetryConstants.DELAY_SHORT;
import static no.nav.dokarkiv.core.storage.RetryConstants.MULTIPLIER_SHORT;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class PdlIdentConsumer implements IdentConsumer {
	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";
	private static final String DEFAULT_CLAIM_OID = "oid";
	private static final String DEFAULT_CLAIM_SUB = "sub";

	private final WebClient webClient;
	private final AzureToken azureToken;
	private final DokarkivProperties dokarkivProperties;
	private final TokenValidationContextHolder tokenValidationContextHolder;

	public PdlIdentConsumer(WebClient webClient, AzureToken azureToken,
							DokarkivProperties dokarkivProperties, TokenValidationContextHolder tokenValidationContextHolder) {
		this.tokenValidationContextHolder = tokenValidationContextHolder;
		this.azureToken = azureToken;
		this.dokarkivProperties = dokarkivProperties;
		this.webClient = webClient.mutate()
				.baseUrl(dokarkivProperties.getEndpoints().getPdl().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.filter(new NavHeadersFilter())
				.build();
	}

	@Retryable(
			include = HttpServerErrorException.class,
			backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT)
	)
	@Override
	public String hentAktoerId(String folkeregisterIdent) throws PersonIkkeFunnetException {

		String ident = this.validateFolkeregisterIdent(folkeregisterIdent);
		PdlResponse pdlResponse = webClient.post()
				.header(HttpHeaders.AUTHORIZATION, azureToken())
				.bodyValue(mapHentAktoerIdForFolkeregisterident(ident))
				.retrieve()
				.bodyToMono(PdlResponse.class)
				.doOnError(this::handleError)
				.block();

		if (pdlResponse.getErrors() == null || pdlResponse.getErrors().isEmpty()) {
			return pdlResponse.getData().getHentIdenter().getIdenter().get(0).getIdent();
		} else {
			if (PERSON_IKKE_FUNNET_CODE.equals(pdlResponse.getErrors().get(0).getExtensions().getCode())) {
				throw new PersonIkkeFunnetException("Fant ikke aktørid for person i pdl.");
			}
			throw new PdlFunctionalException("Kunne ikke hente aktørid for folkeregisterident i pdl. " + pdlResponse.getErrors());
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

		String ident = this.validateFolkeregisterIdent(aktoerId);
		final PdlResponse pdlResponse = webClient.post()
				.header(HttpHeaders.AUTHORIZATION, azureToken())
				.bodyValue(mapHentFolkeregisterIdentForAktoerId(ident))
				.retrieve()
				.bodyToMono(PdlResponse.class)
				.doOnError(this::handleError)
				.block();

		if (pdlResponse.getErrors() == null || pdlResponse.getErrors().isEmpty()) {
			return pdlResponse.getData().getHentIdenter().getIdenter().get(0).getIdent();
		} else {
			if (PERSON_IKKE_FUNNET_CODE.equals(pdlResponse.getErrors().get(0).getExtensions().getCode())) {
				throw new PersonIkkeFunnetException("Fant ikke folkeregisterident for person i pdl.");
			}
			throw new PdlFunctionalException("Kunne ikke hente folkeregisterident for aktørid i pdl. " + pdlResponse.getErrors());
		}
	}

	private PdlRequest mapHentFolkeregisterIdentForAktoerId(final String ident) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("ident", ident);
		return PdlRequest.builder()
				.query("query hentIdenter($ident: ID!) {hentIdenter(ident: $ident, grupper: [FOLKEREGISTERIDENT, NPID] historikk: false) {identer { ident gruppe historisk } } }")
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
		String ident = this.validateFolkeregisterIdent(folkeregisterIdent);

		PdlResponse pdlResponse = webClient.post()
				.header(HttpHeaders.AUTHORIZATION, azureToken())
				.bodyValue(mapHentHistoriskeFolkeregisterIdentForAktoerId(ident))
				.retrieve()
				.bodyToMono(PdlResponse.class)
				.doOnError(this::handleError)
				.block();

		if (pdlResponse.getErrors() == null || pdlResponse.getErrors().isEmpty()) {
			return pdlResponse.getData().getHentIdenter().getIdenter().stream().map(PdlResponse.PdlIdent::getIdent).collect(Collectors.toList());
		} else {
			if (PERSON_IKKE_FUNNET_CODE.equals(pdlResponse.getErrors().get(0).getExtensions().getCode())) {
				throw new PersonIkkeFunnetException("Fant ikke historiske identer for person i pdl.");
			}
			throw new PdlFunctionalException("Kunne ikke hente historiske identer for ident." + pdlResponse.getErrors());
		}
	}

	@Override
	public String hentPersonIdent(String ident, String tema) {

		PdlPersonResponse pdlPersonResponse = webClient.post()
				.header(HttpHeaders.AUTHORIZATION, azureToken())
				.bodyValue(mapHentPersonIdentForId(this.validateFolkeregisterIdent(ident)))
				.retrieve()
				.bodyToMono(PdlPersonResponse.class)
				.doOnError(this::handleError)
				.block();

		if (pdlPersonResponse.getData().getHentPerson() != null && !pdlPersonResponse.getData().getHentPerson().getNavn().isEmpty()) {
			return pdlPersonResponse.getData().getHentPerson().getNavn().get(0).getNavn();
		} else {
			if (pdlPersonResponse.getErrors() == null || pdlPersonResponse.getErrors().isEmpty()) {
				throw new PdlFunctionalException("Person har ikke navn i pdl.");
			} else {
				if (PERSON_IKKE_FUNNET_CODE.equals(pdlPersonResponse.getErrors().get(0).getExtensions().getCode())) {
					throw new PersonIkkeFunnetException("Fant ikke navn for person i pdl.");
				}
			}
			throw new PdlFunctionalException("Kunne ikke hente navn for aktørid i pdl. " + pdlPersonResponse.getErrors());
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

	private String azureToken() {
		TokenValidationContext tokenValidationContext = tokenValidationContextHolder.getTokenValidationContext();
		JwtToken jwtToken = tokenValidationContext.getJwtToken(ISSUER_AZUREV2);
		if (tokenValidationContext.getJwtTokenAsOptional(ISSUER_AZUREV2).isPresent() && isOnBehalfOfToken(jwtToken)) {
			return azureToken.onBehalfOfAccessToken(jwtToken.getTokenAsString(), dokarkivProperties.getEndpoints().getPdl().getScope());
		}
		return azureToken.clientCredentialAccessToken(dokarkivProperties.getEndpoints().getPdl().getScope());
	}

	private boolean isOnBehalfOfToken(JwtToken token) {
		final JwtTokenClaims jwtTokenClaims = token.getJwtTokenClaims();
		return jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_SUB) != null &&
				jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_OID) != null &&
				!jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_SUB).equals(jwtTokenClaims.getStringClaim(DEFAULT_CLAIM_OID));
	}

	String validateFolkeregisterIdent(String ident) {
		if (isBlank(ident)) {
			throw new PersonIkkeFunnetException("Validering av ident feilet fordi verdien er null eller blank.");
		}

		String identTrimmed = ident.trim();

		if (!isNumeric(identTrimmed)) {
			throw new PersonIkkeFunnetException("Validering av ident feilet fordi verdien inneholder bokstaver");
		}

		if (identTrimmed.length() != 13 && identTrimmed.length() != 11) {
			throw new PersonIkkeFunnetException("Validering av ident feilet fordi verdien har lengde " + identTrimmed.length() + ". Akseptert lengde er 11 eller 13");
		}

		return identTrimmed;
	}

	private void handleError(Throwable error) {
		if (error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
			throw new AzureTokenException(
					String.format("Kall mot pdl feilet funksjonelt med statuskode=%s Feilmelding=%s",
							response.getRawStatusCode(),
							response.getMessage()),
					error);
		} else {
			throw new AzureTokenException(
					String.format("Kall mot pdl feilet teknisk med feilmelding=%s", error.getMessage()),
					error);
		}
	}
}
