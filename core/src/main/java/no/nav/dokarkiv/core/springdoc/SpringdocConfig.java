package no.nav.dokarkiv.core.springdoc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER;
import static io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ConditionalOnProperty(
		value = {"springdoc.enabled"},
		havingValue = "true"
)
@Configuration
public class SpringdocConfig {

	@Bean
	public OpenAPI dokarkivApi(@Value("${NAIS_APP_IMAGE:1-SNAPSHOT}") String version) {
		return new OpenAPI()
				.info(new Info()
						.title("Dokarkiv APIer")
						.description("""
								REST-grensesnittene til dokarkiv.
								Vennligst se confluence for utfyllende informasjon.
								""")
						.version(version))
				.externalDocs(new ExternalDocumentation()
						.description("Arkivering i fagarkivet")
						.url("https://confluence.adeo.no/display/BOA/Arkivering+i+fagarkivet"))
				.components(
						new Components()
								.addSecuritySchemes("Authorization",
										new SecurityScheme()
												.type(HTTP)
												.scheme("bearer")
												.bearerFormat("JWT")
												.in(HEADER)
												.description("Eksempel på verdi som skal inn i Value-feltet (Bearer trengs altså ikke å oppgis): 'eyAidH...'")
												.name(AUTHORIZATION)
								)
				)
				.addSecurityItem(
						new SecurityRequirement()
								.addList("Authorization")
				);
	}
}
