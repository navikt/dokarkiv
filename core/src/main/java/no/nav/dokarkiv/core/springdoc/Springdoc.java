package no.nav.dokarkiv.core.springdoc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import no.nav.dokarkiv.core.NavHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class Springdoc {

	@Value("${APP_VERSION:0.0.0}")
	private String version;

	@Bean
	public OpenAPI apiNew() {
		return new OpenAPI()
				.info(new Info()
						.title("Dokarkiv APIer")
						.description("""
								Her dokumenteres REST-grensesnittene til dokarkiv (Joark). Til autentisering brukes to OIDC-token (JWT via OAuth2.0) i hver sin header
								med `Nav-Consumer-Token` (applikasjonsbrukeren sitt token) og `Authorization` (saksbehandleren sitt token). Eksempel på kall med to tokens:

								curl -X PUT "https://dokarkiv-q1.nais.preprod.local/rest/journalpostapi/v1/journalpost/111" -H "accept: */*" -H "Authorization: Bearer eyAidH...", -H "Nav-Consumer-Token: Bearer eyJraWQi..." -H "Content-Type: application/json" -d "{ \\"avsenderMottaker\\": { \\"id\\": \\"string\\", \\"land\\": \\"string\\",...}"
																
								Hvis disse tjenestene blir kalt direkte fra en annen applikasjon hvor saksbehandlertoken ikke er tilgjengjelig er det mulig å autentisere seg med ett OIDC token. Da skal `Authorization` header inneholde applikasjonsbrukeren sitt token og `Nav-Consumer-Token` header ikke settes
								""")
						.version(version))
				.components(
						new Components()
								.addSecuritySchemes("Authorization",
										new SecurityScheme()
												.type(SecurityScheme.Type.HTTP)
												.scheme("bearer")
												.bearerFormat("JWT")
												.in(SecurityScheme.In.HEADER)
												.description("Eksempel på verdi som skal inn i Value-feltet (Bearer trengs altså ikke å oppgis): 'eyAidH...'")
												.name(HttpHeaders.AUTHORIZATION)
								)
								.addSecuritySchemes("Nav-Consumer-Token",
										new SecurityScheme()
												.type(SecurityScheme.Type.APIKEY)
												.scheme("bearer")
												.bearerFormat("JWT")
												.in(SecurityScheme.In.HEADER)
												.description("Eksempel på verdi som skal inn i Value-feltet: 'Bearer eyJraWQi...'")
												.name(NavHeaders.NAV_CONSUMER_TOKEN)
								)
				)
				.addSecurityItem(
						new SecurityRequirement()
								.addList("Authorization")
								.addList("Nav-Consumer-Token")
				);
	}
}
