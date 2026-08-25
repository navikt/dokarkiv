package no.nav.dokarkiv.core.consumer.pdl;

import no.nav.dokarkiv.core.consumer.texas.ExplicitTargetScopeNaisTexasRequestInterceptor;
import no.nav.dokarkiv.core.consumer.texas.NaisTexasConsumer;
import no.nav.dokarkiv.core.exceptions.PdlTechnicalException;
import no.nav.dokarkiv.core.properties.DokarkivProperties;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.HttpSyncGraphQlClient;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static no.nav.dokarkiv.core.consumer.texas.NaisTexasRequestInterceptor.TARGET_SCOPE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class PdlIdentConsumer implements IdentConsumer {

	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";
	// https://pdldocs-navno.msappproxy.net/ekstern/index.html#_dokumenter_hjemmel_vha_tema
	private static final String HEADER_PDL_BEHANDLINGSNUMMER = "behandlingsnummer";
	// https://behandlingskatalog.nais.adeo.no/process/purpose/ARKIVPLEIE/756fd557-b95e-4b20-9de9-6179fb8317e6
	private static final String ARKIVPLEIE_BEHANDLINGSNUMMER = "B315";
	public static final String HENT_AKTOERID_FOR_FOLKEREGISTERIDENT = """
		query hentIdenter($ident: ID!) {
		  hentIdenter(ident: $ident, grupper: AKTORID, historikk: false) {
		    identer {
		      ident
		      gruppe
		      historisk
		    }
		  }
		}
		""";
	public static final String HENT_FOLKEREGISTERIDENT_FOR_AKTOERID = """
		query hentIdenter($ident: ID!) {
		  hentIdenter(ident: $ident, grupper: [FOLKEREGISTERIDENT, NPID], historikk: false) {
		    identer {
		      ident
		      gruppe
		      historisk
		    }
		  }
		}
		""";
	public static final String HENT_PERSON_IDENT_FOR_ID = """
		query hentPerson($ident: ID!) {
		  hentPerson(ident: $ident) {
		    navn(historikk: false) {
		      fornavn
		      mellomnavn
		      etternavn
		    }
		  }
		}""";
	public static final String HENT_ALLE_AKTOERID_FOR_IDENT = """
		query hentIdenter($ident: ID!) {
		  hentIdenter(ident: $ident, grupper: AKTORID, historikk: true) {
		    identer {
		      ident
		      gruppe
		      historisk
		    }
		  }
		}
		""";
	private final HttpSyncGraphQlClient graphQlClient;
	private final String pdlScope;

	public PdlIdentConsumer(RestClient restClient,
							NaisTexasConsumer naisTexasConsumer,
							DokarkivProperties dokarkivProperties) {
		this.pdlScope = dokarkivProperties.getEndpoints().getPdl().getScope();
		this.graphQlClient = HttpSyncGraphQlClient.builder(
				restClient.mutate()
					.baseUrl(dokarkivProperties.getEndpoints().getPdl().getUrl())
					.defaultHeaders((headers) -> {
						headers.setContentType(APPLICATION_JSON);
						headers.set(HEADER_PDL_BEHANDLINGSNUMMER, ARKIVPLEIE_BEHANDLINGSNUMMER);
					})
					// attribute-feltet er av en eller annen grunn ikke
					// tilgjengelig i HttpRequest når nais-texas-interceptoren
					// slår inn. Dette skyldes at attributes ikke blir sendt
					// videre til restclient når man bruker
					// HttpSyncGraphQlTransport. En fiks har blitt gjort i
					// spring-graphql nå, som går ut i release 2.0.6. Inntil
					// videre må interceptoren allerede vite hvilket scope som
					// skal brukes. Dette kan fjernes når spring-graphql er
					// oppdatert til versjon 2.0.6
					.requestInterceptor(new ExplicitTargetScopeNaisTexasRequestInterceptor(naisTexasConsumer, dokarkivProperties.getEndpoints().getPdl().getScope()))
					.build()
			)
			.build();
	}

	@Retryable(includes = PdlTechnicalException.class)
	@Override
	public String hentAktoerId(String folkeregisterIdent) throws PersonIkkeFunnetException {
		try {
			String ident = validateAndTrimIdent(folkeregisterIdent);
			ClientGraphQlResponse graphQlResponse = graphQlClient
				.document(HENT_AKTOERID_FOR_FOLKEREGISTERIDENT)
				.variable("ident", ident)
				.attribute(TARGET_SCOPE, pdlScope)
				.executeSync();

			if (graphQlResponse.getErrors().isEmpty()) {
				PdlResponse.PdlHentIdenter pdlResponse = graphQlResponse.toEntity(PdlResponse.PdlHentIdenter.class);
				return isPdlResponseOrIdenterNull(pdlResponse) ? null : pdlResponse.getHentIdenter().getIdenter().getFirst().getIdent();
			} else {
				if (PERSON_IKKE_FUNNET_CODE.equals(graphQlResponse.getErrors().getFirst().getExtensions().get("code"))) {
					throw new PersonIkkeFunnetException("Fant ikke aktørid for person i pdl.");
				}
				throw new PdlFunctionalException("Kunne ikke hente aktørid for folkeregisterident i pdl. " + graphQlResponse.getErrors());
			}
		} catch (RestClientException e) {
			throw mapError(e);
		}
	}

	@Retryable(includes = PdlTechnicalException.class)
	@Override
	public String hentFolkeregisterIdent(String aktoerId) throws PersonIkkeFunnetException {
		try {
			String ident = validateAndTrimIdent(aktoerId);
			ClientGraphQlResponse graphQlResponse = graphQlClient
				.document(HENT_FOLKEREGISTERIDENT_FOR_AKTOERID)
				.variable("ident", ident)
				.attribute(TARGET_SCOPE, pdlScope)
				.executeSync();

			if (graphQlResponse.getErrors().isEmpty()) {
				PdlResponse.PdlHentIdenter pdlResponse = graphQlResponse.toEntity(PdlResponse.PdlHentIdenter.class);
				return pdlResponse.getHentIdenter().getIdenter().getFirst().getIdent();
			} else {
				if (PERSON_IKKE_FUNNET_CODE.equals(graphQlResponse.getErrors().get(0).getExtensions().get("code"))) {
					throw new PersonIkkeFunnetException("Fant ikke folkeregisterident for person i pdl.");
				}
				throw new PdlFunctionalException("Kunne ikke hente folkeregisterident for aktørid i pdl. " + graphQlResponse.getErrors());
			}
		} catch (RestClientException e) {
			throw mapError(e);
		}
	}

	@Retryable(includes = PdlTechnicalException.class)
	@Override
	public List<String> hentAlleAktoerIdsForIdent(final String ident) throws PersonIkkeFunnetException {
		try {
			String trimmedIdent = validateAndTrimIdent(ident);

			ClientGraphQlResponse graphQlResponse = graphQlClient
				.document(HENT_ALLE_AKTOERID_FOR_IDENT)
				.variable("ident", trimmedIdent)
				.attribute(TARGET_SCOPE, pdlScope)
				.executeSync();

			if (graphQlResponse.getErrors().isEmpty()) {
				PdlResponse.PdlHentIdenter pdlResponse = graphQlResponse.toEntity(PdlResponse.PdlHentIdenter.class);
				return pdlResponse.getHentIdenter().getIdenter()
					.stream().map(PdlResponse.PdlIdent::getIdent).toList();
			} else {
				if (PERSON_IKKE_FUNNET_CODE.equals(graphQlResponse.getErrors().get(0).getExtensions().get("code"))) {
					throw new PersonIkkeFunnetException("Fant ikke historiske aktørIder for person i pdl.");
				}
				throw new PdlFunctionalException("Kunne ikke hente historiske identer for ident." + graphQlResponse.getErrors());
			}
		} catch (RestClientException e) {
			throw mapError(e);
		}
	}

	@Retryable(includes = PdlTechnicalException.class)
	@Override
	public String hentPersonnavn(String ident) {
		try {
			String validateAndTrimIdent = validateAndTrimIdent(ident);
			ClientGraphQlResponse graphQlResponse = graphQlClient
				.document(HENT_PERSON_IDENT_FOR_ID)
				.variable("ident", validateAndTrimIdent)
				.attribute(TARGET_SCOPE, pdlScope)
				.executeSync();

			PdlPersonResponse.PdlHentPersoner pdlPersonResponse = graphQlResponse.toEntity(PdlPersonResponse.PdlHentPersoner.class);
			if (pdlPersonResponse.getHentPerson() != null && !pdlPersonResponse.getHentPerson().getNavn().isEmpty()) {
				return pdlPersonResponse.getHentPerson().getNavn().getFirst().getFulltNavn();
			} else {
				if (graphQlResponse.getErrors().isEmpty()) {
					throw new PdlFunctionalException("Person har ikke navn i pdl.");
				} else {
					if (PERSON_IKKE_FUNNET_CODE.equals(graphQlResponse.getErrors().getFirst().getExtensions().get("code"))) {
						throw new PersonIkkeFunnetException("Fant ikke navn for person i pdl.");
					}
				}
				throw new PdlFunctionalException("Kunne ikke hente navn for aktørid i pdl. " + graphQlResponse.getErrors());
			}
		} catch (RestClientException e) {
			throw mapError(e);
		}
	}

	boolean isPdlResponseOrIdenterNull(PdlResponse.PdlHentIdenter pdlHentIdenter) {
		return pdlHentIdenter == null ||
			pdlHentIdenter.getHentIdenter() == null ||
			pdlHentIdenter.getHentIdenter().getIdenter() == null ||
			pdlHentIdenter.getHentIdenter().getIdenter().isEmpty();
	}

	String validateAndTrimIdent(String ident) {
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

	private RuntimeException mapError(RestClientException error) {
		if (error instanceof HttpClientErrorException clientErrorException) {
			return new PdlFunctionalException(
				"Kall mot pdl feilet funksjonelt med statuskode=%s feilmelding=%s".formatted(
					clientErrorException.getStatusCode(),
					clientErrorException.getMessage()),
				error);
		} else if (error instanceof HttpServerErrorException serverErrorException) {
			return new PdlTechnicalException(
				"Kall mot pdl feilet teknisk med statuskode=%s feilmelding=%s".formatted(
					serverErrorException.getStatusCode(),
					serverErrorException.getMessage()),
				error);
		} else {
			return new PdlTechnicalException(
				"Kall mot pdl feilet teknisk med feilmelding=%s".formatted(error.getMessage()),
				error);
		}
	}
}
