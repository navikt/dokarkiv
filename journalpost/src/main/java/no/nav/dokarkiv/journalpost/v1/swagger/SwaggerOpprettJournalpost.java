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
		summary = "Oppretter en journalpost i fagarkivet, med eller uten dokumenter"
)
@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Journalpost opprettet"),
		@ApiResponse(responseCode = "400", description = "Kan ikke opprette journalpost", content = @Content),
		@ApiResponse(responseCode = "401", description = "Mangler tilgang til å opprette ny journalpost. Ugyldig OIDC token. Denne feilen gis dersom tokenet ikke har riktig format eller er utgått.", content = @Content),
		@ApiResponse(responseCode = "403", description = "Bruker mangler tilgang til å opprette journalpost på tema", content = @Content),
		@ApiResponse(responseCode = "409", description = "Journalpost med angitt eksternReferanseId eksisterer allerede. Ingen journalpost ble opprettet."),
		@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
})
public @interface SwaggerOpprettJournalpost {
}
