package no.nav.dokarkiv.journalpost.v1.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD})
@Retention(RUNTIME)
@Documented
@Operation(
		summary = "Avslutter alle saker på et tema/fagområde/arkivdel."
)
@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Alle saker for tema har blitt avsluttet.", content = @Content),
		@ApiResponse(responseCode = "400", description = "Validering av request feilet.", content = @Content),
		@ApiResponse(responseCode = "401", description = "Ugyldig Bearer-token. Token kan være utgått, ha feil format, eller mangle nødvendig rolle.", content = @Content),
		@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
})
public @interface SwaggerAvsluttAlleSakerPaaTema {
}