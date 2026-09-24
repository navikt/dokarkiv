package no.nav.dokarkiv.journalpost.v1.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import no.nav.dokarkiv.journalpost.v1.api.dokumentfil.DokumentFilResponse;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
		summary = "Last opp en DokumentFil",
		description = """
				Mottar en dokumentFil og lagrer den. Krever headerne \
				`Content-Type`, `Content-Digest` og `Idempotency-Key`. `Idempotency-Key` er fagsystemets egen referanse til dokumentet og \
				brukes for å hindre at samme fil lastes opp flere ganger""",
		operationId = "lastOppDokumentFil"
)
@ApiResponses(value = {
		@ApiResponse(
				responseCode = "201",
				description = "Dokumentet er lastet opp eller teknisk retry der dokumentet allerede er lastet opp.",
				content = @Content(
						mediaType = "application/json",
						schema = @Schema(implementation = DokumentFilResponse.class)
				)
		),
		@ApiResponse(responseCode = "400", description = "Requesten mangler påkrevde headere, headerne har ugyldig format eller dokumentets `Content-Digest` matcher ikke innholdet i payload.", content = @Content),
		@ApiResponse(responseCode = "401", description = "Ugyldig token. Denne feilen gis dersom tokenet ikke har riktig format eller er utgått.", content = @Content),
		@ApiResponse(responseCode = "409", description = "`Idempotency-Key` brukes allerede av en opplasting som pågår.", content = @Content),
		@ApiResponse(responseCode = "422", description = "`Idempotency-Key` er allerede brukt for et dokument med et annet innhold.", content = @Content),
		@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
})
public @interface SwaggerLastOppDokumentFil {
}
