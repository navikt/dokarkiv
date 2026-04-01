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
	summary = "Skjermer et dokument, og alle journalposter dokumentet er knyttet til med mindre de aktuelle journalpostene har andre, ikke-skjermede dokumenter"
)
@ApiResponses(value = {
	@ApiResponse(responseCode = "204", description = "Dokumentet ble skjermet"),
	@ApiResponse(responseCode = "401", description = "Konsument har ikke tilgang til å kalle tjenesten.", content = @Content),
	@ApiResponse(responseCode = "403", description = "Konsument har ikke tilgang til å kalle tjenesten", content = @Content),
	@ApiResponse(responseCode = "404", description = "Dokumentet finnes ikke eller er utilgjengelig", content = @Content),
	@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
})
public @interface SwaggerSkjermDokument {
}
