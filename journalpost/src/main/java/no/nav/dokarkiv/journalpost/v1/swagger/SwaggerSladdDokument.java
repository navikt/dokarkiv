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
	Sladder et dokument ved å lagre en opplastet sladdet variant (application/pdf) og skjerme arkivvarianten.
	Skjermingen utledes fra dokumentets journalpostrelasjon. Eventuell skjerming på tilknyttede journalposter fjernes.
	"""
)
@ApiResponses(value = {
	@ApiResponse(responseCode = "204", description = "Dokumentet ble sladdet"),
	@ApiResponse(responseCode = "400", description = "Ugyldig request. Se feilmelding", content = @Content),
	@ApiResponse(responseCode = "401", description = "Konsument har ikke tilgang til å kalle tjenesten.", content = @Content),
	@ApiResponse(responseCode = "403", description = "Konsument har ikke tilgang til å kalle tjenesten", content = @Content),
	@ApiResponse(responseCode = "404", description = "Dokumentet finnes ikke eller er utilgjengelig", content = @Content),
	@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
})
public @interface SwaggerSladdDokument {
}
