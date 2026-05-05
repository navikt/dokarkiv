package no.nav.dokarkiv.internal.settbrevdata;

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
		summary = "Setter brevdata på en Journalpost med status under arbeid eller status reservert"
)
@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Dokument finnes fra før eller dokument oppdateres"),
		@ApiResponse(responseCode = "201", description = "Dokument opprettet"),
		@ApiResponse(responseCode = "400", description = "Feil i path. variantFormat er ikke ARKIV eller PRODUKSJON, Content-Type header matcher ikke variantFormat", content = @Content),
		@ApiResponse(responseCode = "401", description = "Konsument har ugyldig token eller har ikke role api_intern_brevserver", content = @Content),
		@ApiResponse(responseCode = "404", description = "Journalpost ikke funnet eller hoveddokument ikke funnet, ", content = @Content),
		@ApiResponse(responseCode = "409", description = "Feil journalstatus. Må være reservert (R) eller under arbeid (D)", content = @Content),
		@ApiResponse(responseCode = "415", description = "Content-Type er ikke en av application/rtf eller application/pdf", content = @Content),
		@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
})
public @interface SwaggerSettBrevdata {
}
