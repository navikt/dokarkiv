package no.nav.dokarkiv.core.springdoc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

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
								Vennligst se confluence for utfyllende informasjon. [https://confluence.adeo.no/display/BOA/Arkivering+i+fagarkivet](Arkivering i fagarkivet)
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
				)
				.addSecurityItem(
						new SecurityRequirement()
								.addList("Authorization")
				);
	}
}
