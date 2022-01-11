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
		summary = "Tar en inngående journalpost ut av ordinær saksbehandling fordi det ikke er mulig å identifisere hvilken bruker dokumentene er knyttet til"
)
@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK"),
		@ApiResponse(responseCode = "400", description = "Feil i aksjonslogg", content = @Content),
		@ApiResponse(responseCode = "401", description = "Ugyldig OIDC token. Denne feilen gis dersom tokenet ikke har riktig format eller er utgått.", content = @Content),
		@ApiResponse(responseCode = "404", description = "Journalpost ikke funnet", content = @Content),
		@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
})
public @interface SwaggerSettUkjentBruker {
}
