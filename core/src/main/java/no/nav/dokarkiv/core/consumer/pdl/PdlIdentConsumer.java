package no.nav.dokarkiv.core.consumer.pdl;

import no.nav.dokarkiv.core.consumer.azure.AzureToken;
import no.nav.dokarkiv.core.consumer.azure.WebClientAzureAuthentication;
import no.nav.dokarkiv.core.exceptions.PdlTechnicalException;
import no.nav.dokarkiv.core.properties.DokarkivProperties;
import no.nav.dokarkiv.core.util.NavHeadersFilter;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.cache.CacheConfig.HISTORISKE_IDENTER;
import static no.nav.dokarkiv.core.storage.RetryConstants.DELAY_SHORT;
import static no.nav.dokarkiv.core.storage.RetryConstants.MULTIPLIER_SHORT;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class PdlIdentConsumer implements IdentConsumer {

	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";
	private static final String HEADER_PDL_TEMA = "Tema";
	// https://pdldocs-navno.msappproxy.net/ekstern/index.html#_dokumenter_hjemmel_vha_tema
	private static final String HEADER_PDL_BEHANDLINGSNUMMER = "behandlingsnummer";
	// https://behandlingskatalog.nais.adeo.no/process/purpose/ARKIVPLEIE/756fd557-b95e-4b20-9de9-6179fb8317e6
	private static final String ARKIVPLEIE_BEHANDLINGSNUMMER = "B315";

	private final WebClient webClient;

	public PdlIdentConsumer(WebClient webClient,
							DokarkivProperties dokarkivProperties,
							AzureToken azureToken) {
		this.webClient = webClient.mutate()
				.baseUrl(dokarkivProperties.getEndpoints().getPdl().getUrl())
				.defaultHeaders((headers) -> {
					headers.setContentType(APPLICATION_JSON);
					headers.set(HEADER_PDL_BEHANDLINGSNUMMER, ARKIVPLEIE_BEHANDLINGSNUMMER);
				})
				.filter(new NavHeadersFilter())
				.filter(new WebClientAzureAuthentication(azureToken, dokarkivProperties.getEndpoints().getPdl().getScope()))
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
				.bodyValue(mapHentAktoerIdForFolkeregisterident(ident))
				.retrieve()
				.bodyToMono(PdlResponse.class)
				.doOnError(this::handleError)
				.block();

		if (pdlResponse.getErrors() == null || pdlResponse.getErrors().isEmpty()) {
			return isPdlResponseOrIdenterNull(pdlResponse) ? null : pdlResponse.getData().getHentIdenter().getIdenter().get(0).getIdent();
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

	@Retryable(
			include = HttpServerErrorException.class,
			backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT)
	)
	@Cacheable(HISTORISKE_IDENTER)
	@Override
	public List<String> hentHistoriskeFolkeregisterIdenter(String folkeregisterIdent) throws PersonIkkeFunnetException {
		String ident = this.validateFolkeregisterIdent(folkeregisterIdent);

		PdlResponse pdlResponse = webClient.post()
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

	@Retryable(
			include = HttpServerErrorException.class,
			backoff = @Backoff(delay = DELAY_SHORT, multiplier = MULTIPLIER_SHORT)
	)
	@Override
	public String hentPersonnavn(String ident, String tema) {

		ResponseEntity<PdlPersonResponse> pdlPersonResponse = webClient.post()
				.header(HEADER_PDL_TEMA, tema)
				.bodyValue(mapHentPersonIdentForId(this.validateFolkeregisterIdent(ident)))
				.retrieve()
				.toEntity(PdlPersonResponse.class)
				.doOnError(this::handleError).block();

		if (pdlPersonResponse.getBody().getData().getHentPerson() != null && !pdlPersonResponse.getBody().getData().getHentPerson().getNavn().isEmpty()) {
			return pdlPersonResponse.getBody().getData().getHentPerson().getNavn().get(0).getFulltNavn();
		} else {
			if (pdlPersonResponse.getBody().getErrors() == null || pdlPersonResponse.getBody().getErrors().isEmpty()) {
				throw new PdlFunctionalException("Person har ikke navn i pdl.");
			} else {
				if (PERSON_IKKE_FUNNET_CODE.equals(pdlPersonResponse.getBody().getErrors().get(0).getExtensions().getCode())) {
					throw new PersonIkkeFunnetException("Fant ikke navn for person i pdl.");
				}
			}
			throw new PdlFunctionalException("Kunne ikke hente navn for aktørid i pdl. " + pdlPersonResponse.getBody().getErrors());
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

	boolean isPdlResponseOrIdenterNull(PdlResponse pdlResponse) {
		return Objects.isNull(pdlResponse.getData().getHentIdenter()) ||
				Objects.isNull(pdlResponse.getData().getHentIdenter().getIdenter()) ||
				pdlResponse.getData().getHentIdenter().getIdenter().isEmpty();
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
			throw new PdlFunctionalException(
					String.format("Kall mot pdl feilet funksjonelt med statuskode=%s Feilmelding=%s",
							response.getRawStatusCode(),
							response.getMessage()),
					error);
		} else {
			throw new PdlTechnicalException(
					String.format("Kall mot pdl feilet teknisk med feilmelding=%s", error.getMessage()),
					error);
		}
	}
}
