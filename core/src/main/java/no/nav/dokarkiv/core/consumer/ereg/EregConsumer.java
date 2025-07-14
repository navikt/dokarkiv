package no.nav.dokarkiv.core.consumer.ereg;

import no.nav.dokarkiv.core.exceptions.EregFunctionalException;
import no.nav.dokarkiv.core.exceptions.EregTechnicalException;
import no.nav.dokarkiv.core.properties.DokarkivProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
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

	public EregResponse hentOrganisasjon(String organisasjonsnummer) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/{organisasjonsnummer}/noekkelinfo")
						.build(organisasjonsnummer))
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> {
					if (res.getStatusCode().is4xxClientError()) {
						throw new EregFunctionalException(format("Klarte ikke hente organisasjon med organisasjonsnummer=%s feilmelding=%s", organisasjonsnummer, extractErrorMessage(res)));
					}
					throw new EregTechnicalException(format("Klarte ikke hente organisasjon med organisasjonsnummer=%s feilmelding=%s", organisasjonsnummer, extractErrorMessage(res)));
				})
				.body(EregResponse.class);
	}

	public String extractErrorMessage(ClientHttpResponse response) throws IOException {
		return new BufferedReader(
				new InputStreamReader(response.getBody(), UTF_8))
				.lines()
				.collect(Collectors.joining("\n"));
	}
}
