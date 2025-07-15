package no.nav.dokarkiv.core.consumer.ereg;

import no.nav.dokarkiv.core.exceptions.EregFunctionalException;
import no.nav.dokarkiv.core.exceptions.EregTechnicalException;
import no.nav.dokarkiv.core.properties.DokarkivProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class EregConsumer {

	private final RestClient restClient;

	public EregConsumer(RestClient.Builder restClientBuilder,
						DokarkivProperties dokarkivProperties) {
		this.restClient = restClientBuilder
				.baseUrl(dokarkivProperties.getEreg().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}

	public EregResponse hentOrganisasjonnavn(String organisasjonsnummer) {
		ResponseEntity<EregResponse> entity = restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/{organisasjonsnummer}/noekkelinfo")
						.build(organisasjonsnummer))
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> {
					if (res.getStatusCode().is4xxClientError()) {
						throw new EregFunctionalException(format("Klarte ikke hente organisasjon med organisasjonsnummer=%s feilmelding=%s", organisasjonsnummer, res.getStatusText()));
					}
					throw new EregTechnicalException(format("Klarte ikke hente organisasjon med organisasjonsnummer=%s feilmelding=%s", organisasjonsnummer, res.getStatusText()));
				})
				.toEntity(new ParameterizedTypeReference<EregResponse>() {
				});

		if (!entity.getStatusCode().is2xxSuccessful()) {
			throw new EregTechnicalException(format("Klarte ikke hente organisasjon med organisasjonsnummer=%s, statuskode=%s", organisasjonsnummer, entity.getStatusCode()));
		}
		return entity.getBody();
	}
}
