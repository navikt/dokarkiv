package no.nav.dokarkiv.journalpost.v1.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import no.nav.dokarkiv.core.exceptions.ApplicationProblemDetail;
import no.nav.dokarkiv.journalpost.v1.api.dokumentfil.InitierDokumentFilOpplastingResponse;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
		summary = "Initier direkte opplasting av dokumentFil til objektlager",
		description = """
				Oppretter en kortlivet signert PUT URI for direkte opplasting av dokument til objektlager.
				Utløpstid er 3600 sekunder.
				
				Klienten må behandle PUT URI som en secret.
				
				Klienten må sende avtalte headere når de laster opp dokumentet til PUT URI:
				
				* `Content-Type: {request.mediaType}`
				* `Content-Length: {request.stoerrelse}`
				* `x-goog-hash: crc32c={request.crc32c}`
				
				Samme `Idempotency-Key` og `crc32c` returnerer samme PUT URI og utløpstid.
				""",
		operationId = "initierDokumentFilOpplasting"
)
@ApiResponses({
		@ApiResponse(responseCode = "201", description = "Metadata er opprettet og opplasting kan startes",
				content = @Content(mediaType = "application/json",
						schema = @Schema(implementation = InitierDokumentFilOpplastingResponse.class),
						examples = @ExampleObject(value = """
							{
							  "dokumentFilId": "01a086a4-7cfa-7f7f-a19a-1339a4028587",
							  "eksternDokumentReferanseId": "41ff35f0-dae9-41f8-80b0-4137592c16ca",
							  "mediaType": "application/pdf",
							  "crc32c": "n03x6A==",
							  "stoerrelse": 482133,
							  "tilstand": "UNDER_OPPLASTING",
							  "datoOpprettet": "2026-11-03T15:20:00.000",
							  "datoSistEndret": "2026-11-03T15:20:00.000",
							  "opplasting": {
								"url": "https://storage.googleapis.com/bucket/dokument-id?signature=...",
								"utloeper": "2026-11-03T16:20:00.000"
							  }
							}
							"""))),
		@ApiResponse(responseCode = "400", description = "Felt i request eller `Idempotency-Key` har ugyldig format",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationProblemDetail.class))),
		@ApiResponse(responseCode = "401", description = "Ugyldig token",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationProblemDetail.class))),
		@ApiResponse(responseCode = "403", description = "Token mangler tilgang",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationProblemDetail.class))),
		@ApiResponse(responseCode = "422", description = """
				`Idempotency-Key` er allerede brukt med en annen crc32c, tilstanden er ikke `UNDER_OPPLASTING` eller `Idempotency-Key` er brukt av `POST` tjenesten.
				""",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationProblemDetail.class))),
		@ApiResponse(responseCode = "500", description = "Intern feil ved opprettelse av signert URL",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationProblemDetail.class)))
})
public @interface SwaggerInitierDokumentFilOpplasting {
}
