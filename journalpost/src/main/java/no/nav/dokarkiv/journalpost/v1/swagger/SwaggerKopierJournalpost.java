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
		summary = "Kopierer en journalpost i fagarkivet"
)
@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Journalpost kopiert"),
		@ApiResponse(responseCode = "400", description = "Kan ikke kopiere journalpost", content = @Content),
		@ApiResponse(responseCode = "401", description = "Mangler tilgang til å kopiere ny journalpost. Ugyldig access token", content = @Content),
		@ApiResponse(responseCode = "404", description = "Journalpost finnes ikke eller er utilgjengelig", content = @Content),
		@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
})
public @interface SwaggerKopierJournalpost {
}
