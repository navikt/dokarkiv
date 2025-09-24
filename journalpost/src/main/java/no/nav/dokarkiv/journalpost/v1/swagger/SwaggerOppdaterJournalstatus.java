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
		summary = """
			Endre status for en journalpost. Endepunktet er kun tilgjengelig for brukere i Nav Fagpost, autentisert med et EntraID OBO-token.
			Brukeren må være medlem i EntraID-gruppe for joark vedlikehold"""
)
@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Status på journalpost er endret."),
		@ApiResponse(responseCode = "400", description = "Validering av input feilet.", content = @Content),
		@ApiResponse(responseCode = "401", description = "Ugyldig EntraID token.", content = @Content),
		@ApiResponse(responseCode = "403", description = "Manglende claims i token / Manglende EntraID gruppemedlemskap.", content = @Content),
		@ApiResponse(responseCode = "500", description = "Internal server error.", content = @Content)
})
public @interface SwaggerOppdaterJournalstatus {
}
