package no.nav.dokarkiv.core.consumer.pdl;

import no.nav.dokarkiv.core.consumer.azure.CacheAzureTokenClient;
import no.nav.dokarkiv.core.exceptions.PdlTechnicalException;
import no.nav.dokarkiv.core.properties.DokarkivProperties;
import no.nav.dokarkiv.core.util.NavHeadersFilter;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.cache.CacheConfig.HISTORISKE_IDENTER;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class PdlIdentConsumer implements IdentConsumer {

	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";
	private static final String TEMA = "Tema";

	private final WebClient webClient;

	public PdlIdentConsumer(WebClient webClient,
							DokarkivProperties dokarkivProperties,
							CacheAzureTokenClient pdlAzureTokenCache) {
		this.webClient = webClient.mutate()
				.baseUrl(dokarkivProperties.getEndpoints().getPdl().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.filter(new NavHeadersFilter())
				.filter(new PdlWebClientAzureAuthentication(pdlAzureTokenCache, dokarkivProperties))
				.build();
	}

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

	@Override
	public String hentPersonIdent(String ident, String tema) {

		PdlPersonResponse pdlPersonResponse = webClient.post()
				.header(TEMA, tema)
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
