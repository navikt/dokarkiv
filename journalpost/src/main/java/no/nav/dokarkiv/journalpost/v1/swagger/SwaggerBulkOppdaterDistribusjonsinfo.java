package no.nav.dokarkiv.journalpost.v1.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
		summary = "Fullfører journalføringen og låser journalposten for senere endringer"
)
@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK - Journalposten fikk status ekspedert"),
		@ApiResponse(responseCode = "400", description = """
							Kan bare sette status ekspedert, når:
							* Journalpost er UTGÅENDE
							* Journalpost har status FS eller FL
							* Journalpost har en saksrelasjon som ikke er feilregistrert
						""",
				content = @Content
		),
		@ApiResponse(responseCode = "401", description = "Ugyldig OIDC token. Denne feilen gis dersom tokenet ikke har riktig format eller er utgått.", content = @Content),
		@ApiResponse(responseCode = "403", description = "Konsument har ikke tilgang til å ekspedere journalpost", content = @Content),
		@ApiResponse(responseCode = "404", description = "Journalpost ikke funnet", content = @Content),
		@ApiResponse(responseCode = "500", description = "Uventet teknisk feil", content = @Content)
})
public @interface SwaggerBulkOppdaterDistribusjonsinfo {
}
