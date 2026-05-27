package no.nav.dokarkiv.journalpost.v1.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import no.nav.dokarkiv.journalpost.v1.api.SlettebestillingRequest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
		summary = "Bestill sletting av et dokument",
		description = "Bestiller sletting av dokumentet identifisert med dokumentInfoId i URL.",
		operationId = "bestillSletting",
		tags = {"Slettebestilling"},
		security = @SecurityRequirement(name = "bearer-token"),
		requestBody = @RequestBody(
				required = true,
				description = "Payload for bestilling av sletting.",
				content = @Content(
						mediaType = "application/json",
						schema = @Schema(implementation = SlettebestillingRequest.class),
						examples = @ExampleObject(value = """
							{
							  "hjemmel": "POL",
							  "begrunnelse": "Jira: SLETT-1234"
							}
							""")
				)
		)
)
@ApiResponses(value = {
		@ApiResponse(
				responseCode = "200",
				description = "Slettebestilling er opprettet. Responsen er ID til opprettet bestilling.",
				content = @Content(
						mediaType = "application/json",
						schema = @Schema(type = "integer", format = "int64", example = "12345")
				)
		),
		@ApiResponse(responseCode = "400", description = "Bestilling av sletting kan ikke gjøres på grunn av feil med requesten. Se feilmelding.", content = @Content),
		@ApiResponse(responseCode = "401", description = "Ugyldig OIDC token. Denne feilen gis dersom tokenet ikke har riktig format eller er utgått.", content = @Content),
		@ApiResponse(responseCode = "403", description = "Den som har sendt requesten har ikke tilgang til å bestille sletting av den forespurte typen", content = @Content),
		@ApiResponse(responseCode = "404", description = "Dokumentet finnes ikke", content = @Content),
		@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
})
public @interface SwaggerBestillSletting {
}
