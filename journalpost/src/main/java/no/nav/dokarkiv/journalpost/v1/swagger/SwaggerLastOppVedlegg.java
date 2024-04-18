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
		summary = "Laster opp et dokument som vedlegg til en journalpost"
)
@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Dokument lagt til som vedlegg."),
		@ApiResponse(responseCode = "400", description = "Validering av input feilet.", content = @Content),
		@ApiResponse(responseCode = "401", description = "Ugyldig OIDC token. Denne feilen gis dersom tokenet ikke har riktig format eller er utgått.", content = @Content),
		@ApiResponse(responseCode = "409", description = "Journalposten ikke er under arbeid (status=D), mangler hoveddokument, eller et dokument med samme filnavn eksisterer allerede som vedlegg.", content = @Content),
		@ApiResponse(responseCode = "500", description = "Internal server error.", content = @Content)
})
public @interface SwaggerLastOppVedlegg {
}
