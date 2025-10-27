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
		summary = "Oppretter en ny journalpost og setter den eksisterende journalsposten til utgått. Tjenesten er kun tilgjengelig for ansatte i Nav Fagpost."
)
@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Journalpost splittet", content = @Content),
		@ApiResponse(responseCode = "400", description = "Kan ikke splitte journalpost", content = @Content),
		@ApiResponse(responseCode = "401", description = "Mangler tilgang til å splitte journalpost. Ugyldig access token", content = @Content),
		@ApiResponse(responseCode = "404", description = "Journalpost eller dokument(er) finnes ikke", content = @Content),
		@ApiResponse(responseCode = "409", description = "Journalpost med angitt eksternReferanseId eksisterer allerede. Ingen ny journalpost ble opprettet."),
		@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
})
public @interface SwaggerSplittJournalpost {
}
