package no.nav.dokarkiv.core.swagger;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.DocExpansion;
import springfox.documentation.swagger.web.ModelRendering;
import springfox.documentation.swagger.web.OperationsSorter;
import springfox.documentation.swagger.web.TagsSorter;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	@Value("${APP_VERSION:0.0.0}")
	private String version;

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
				.paths(PathSelectors.any())
				.build()
				.useDefaultResponseMessages(false)
				.apiInfo(apiInfo())
				.securitySchemes(Lists.newArrayList(apiKey(), consumerToken()));
	}

	@Bean
	UiConfiguration uiConfig() {
		return UiConfigurationBuilder.builder()
				.deepLinking(true)
				.displayOperationId(false)
				.defaultModelsExpandDepth(1)
				.defaultModelExpandDepth(1)
				.defaultModelRendering(ModelRendering.EXAMPLE)
				.displayRequestDuration(false)
				.docExpansion(DocExpansion.NONE)
				.filter(false)
				.maxDisplayedTags(null)
				.operationsSorter(OperationsSorter.ALPHA)
				.showExtensions(true)
				.tagsSorter(TagsSorter.ALPHA)
				.validatorUrl(null)
				.build();
	}

	private ApiInfo apiInfo() {
		return new ApiInfo(
				"Dokarkiv APIer",
				"Her dokumenteres REST-grensesnittene til dokarkiv (Joark). Til autentisering brukes to OIDC-token (JWT via OAuth2.0) i hver sin header" +
						"\nmed `Nav-Consumer-Token` (applikasjonsbrukeren sitt token) og `Authorization` (saksbehandleren sitt token). Eksempel på kall med to tokens:\n" +
                        "\n" +
                        "curl -X PUT \"https://dokarkiv-q1.nais.preprod.local/rest/journalpostapi/v1/journalpost/111\" -H \"accept: */*\" -H \"Authorization: Bearer eyAidH...\", -H \"Nav-Consumer-Token: Bearer eyJraWQi...\" -H \"Content-Type: application/json\" -d \"{ \\\"avsenderMottaker\\\": { \\\"id\\\": \\\"string\\\", \\\"land\\\": \\\"string\\\",...}\"\n" +
                        "\n" +
						"Hvis disse tjenestene blir kalt direkte fra en annen applikasjon hvor saksbehandlertoken ikke tilgjengjelig er det mulig å autentisere seg med en OIDC token ved bruk av Authorization header med applikasjonsbrukeren sitt token" +
						"",
				version,
				"",
				new Contact("Team Dokument", "", ""),
				"", "", Collections.emptyList());
	}

	private ApiKey apiKey() {

		return new ApiKey("Authorization", HttpHeaders.AUTHORIZATION, "header");
	}

	private ApiKey consumerToken() {

		return new ApiKey("NavConsumerToken", HttpHeaders.AUTHORIZATION, "header");
	}


}
