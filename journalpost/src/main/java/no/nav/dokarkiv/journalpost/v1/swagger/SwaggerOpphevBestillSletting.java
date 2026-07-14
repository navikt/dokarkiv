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
		summary = "Opphev bestilt sletting av dokument",
		description = "Opphever aktiv slettebestilling for dokumentet identifisert med dokumentInfoId i URL.",
		operationId = "opphevBestillSletting"
)
@ApiResponses(value = {
	@ApiResponse(responseCode = "204", description = "Slettebestillingen ble avbrutt", content = @Content),
	@ApiResponse(responseCode = "400", description = "Slettebestillingen kan ikke avbrytes, fordi den allerede er gjennomført.", content = @Content),
	@ApiResponse(responseCode = "401", description = "Ugyldig OIDC token. Denne feilen gis dersom tokenet ikke har riktig format eller er utgått.", content = @Content),
	@ApiResponse(responseCode = "403", description = "Den som har sendt requesten har ikke tilgang til å oppheve slettebestillingen", content = @Content),
	@ApiResponse(responseCode = "404", description = "Dokumentet eller slettebestillingen finnes ikke", content = @Content),
	@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
})
public @interface SwaggerOpphevBestillSletting {
}
